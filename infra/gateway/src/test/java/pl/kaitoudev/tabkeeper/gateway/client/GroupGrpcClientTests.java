package pl.kaitoudev.tabkeeper.gateway.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.ServerCalls;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import pl.kaitoudev.tabkeeper.gateway.domain.group.GroupDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.GroupRole;
import pl.kaitoudev.tabkeeper.gateway.domain.group.GroupSummaryDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.InvitationAcceptedDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.InvitationCreatedDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.InvitationDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.InvitationStatus;
import pl.kaitoudev.tabkeeper.gateway.domain.group.MembershipDetails;
import pl.kaitoudev.tabkeeper.proto.group.v1.AcceptGroupInvitationResponse;
import pl.kaitoudev.tabkeeper.proto.group.v1.CheckMembershipResponse;
import pl.kaitoudev.tabkeeper.proto.group.v1.GetGroupsForUserResponse;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupInfo;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupMember;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupServiceGrpc;
import pl.kaitoudev.tabkeeper.proto.group.v1.InvitationInfo;
import pl.kaitoudev.tabkeeper.proto.group.v1.InviteUserToGroupResponse;
import pl.kaitoudev.tabkeeper.proto.group.v1.PerUserGroupInfo;
import pl.kaitoudev.tabkeeper.shared.grpc.GrpcMappingUtils;

/** Exercises request construction and response mapping over real in-process gRPC calls. */
class GroupGrpcClientTests {
  /** Stable identifiers shared by requests and responses. */
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID MEMBER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
  private static final UUID INVITATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

  /** Fixture name and timestamp retain nanosecond precision during conversion. */
  private static final String GROUP_NAME = "Road trip";

  private static final Instant CREATED_AT = Instant.parse("2026-11-14T12:00:00.123456789Z");

  /** Maximum command deadline in the client. */
  private static final long MAX_DEADLINE_SECONDS = 5;

  /** Retrieval maps members, roles, UUIDs, and timestamps into gateway types. */
  @Test
  void getGroupMapsMembersToTypedDomainDetails() throws IOException {
    withResponse(
        GroupServiceGrpc.getGetGroupMethod(),
        groupInfo(),
        request -> assertEquals(GROUP_ID.toString(), request.getGroupId()),
        client -> {
          GroupDetails result = client.getGroup(GROUP_ID.toString());
          assertEquals(GROUP_ID, result.groupId());
          assertEquals(GROUP_NAME, result.groupName());
          assertEquals(CREATED_AT, result.createdAt());
          assertEquals(OWNER_ID, result.members().getFirst().userId());
          assertEquals(GroupRole.OWNER, result.members().getFirst().role());
          assertEquals(CREATED_AT, result.members().getFirst().joinedAt());
        });
  }

  /** Creation sends the supplied owner and name and maps the service response. */
  @Test
  void createGroupSendsOwnerAndName() throws IOException {
    withResponse(
        GroupServiceGrpc.getCreateGroupMethod(),
        groupInfo(),
        request -> {
          assertEquals(OWNER_ID.toString(), request.getOwnerId());
          assertEquals(GROUP_NAME, request.getGroupName());
        },
        client ->
            assertEquals(GROUP_ID, client.createGroup(OWNER_ID.toString(), GROUP_NAME).groupId()));
  }

  /** User-relative summaries preserve ownership and creation time. */
  @Test
  void getGroupsForUserMapsOwnershipSummaries() throws IOException {
    GetGroupsForUserResponse response =
        GetGroupsForUserResponse.newBuilder()
            .setUserId(OWNER_ID.toString())
            .addGroupsOfUser(
                PerUserGroupInfo.newBuilder()
                    .setGroupId(GROUP_ID.toString())
                    .setGroupName(GROUP_NAME)
                    .setIsOwner(true)
                    .setCreatedTs(GrpcMappingUtils.toProtobufTimestamp(CREATED_AT)))
            .build();
    withResponse(
        GroupServiceGrpc.getGetGroupsForUserMethod(),
        response,
        request -> assertEquals(OWNER_ID.toString(), request.getUserId()),
        client ->
            assertEquals(
                List.of(new GroupSummaryDetails(GROUP_ID, GROUP_NAME, true, CREATED_AT)),
                client.getGroupsForUser(OWNER_ID.toString())));
  }

  /** An empty upstream group list stays empty. */
  @Test
  void getGroupsForUserPreservesEmptyResults() throws IOException {
    withResponse(
        GroupServiceGrpc.getGetGroupsForUserMethod(),
        GetGroupsForUserResponse.getDefaultInstance(),
        request -> assertEquals(MEMBER_ID.toString(), request.getUserId()),
        client -> assertTrue(client.getGroupsForUser(MEMBER_ID.toString()).isEmpty()));
  }

  /** Membership checks preserve both flags and the identifiers returned upstream. */
  @Test
  void checkMembershipMapsMembershipAndOwnership() throws IOException {
    CheckMembershipResponse response =
        CheckMembershipResponse.newBuilder()
            .setGroupId(GROUP_ID.toString())
            .setUserId(MEMBER_ID.toString())
            .setIsMember(true)
            .setIsOwner(false)
            .build();
    withResponse(
        GroupServiceGrpc.getCheckMembershipMethod(),
        response,
        request -> {
          assertEquals(GROUP_ID.toString(), request.getGroupId());
          assertEquals(MEMBER_ID.toString(), request.getUserId());
        },
        client ->
            assertEquals(
                new MembershipDetails(GROUP_ID, MEMBER_ID, true, false),
                client.checkMembership(GROUP_ID.toString(), MEMBER_ID.toString())));
  }

  /** Invitation creation retains the inviter and invited-user identities. */
  @Test
  void inviteUserToGroupMapsCreatedInvitationIdentifiers() throws IOException {
    InviteUserToGroupResponse response =
        InviteUserToGroupResponse.newBuilder()
            .setInvitationId(INVITATION_ID.toString())
            .setGroupId(GROUP_ID.toString())
            .setInvitedByUserId(OWNER_ID.toString())
            .setInvitedUserId(MEMBER_ID.toString())
            .build();
    withResponse(
        GroupServiceGrpc.getInviteUserToGroupMethod(),
        response,
        request -> {
          assertEquals(GROUP_ID.toString(), request.getGroupId());
          assertEquals(OWNER_ID.toString(), request.getInvitedByUserId());
          assertEquals(MEMBER_ID.toString(), request.getInvitedUserId());
        },
        client ->
            assertEquals(
                new InvitationCreatedDetails(INVITATION_ID, GROUP_ID, OWNER_ID, MEMBER_ID),
                client.inviteUserToGroup(
                    GROUP_ID.toString(), OWNER_ID.toString(), MEMBER_ID.toString())));
  }

  /** A pending invitation does not acquire an artificial epoch acceptance timestamp. */
  @Test
  void getInvitationPreservesAbsentAcceptanceTimestamp() throws IOException {
    withResponse(
        GroupServiceGrpc.getGetInvitationMethod(),
        invitationInfo().build(),
        request -> assertEquals(INVITATION_ID.toString(), request.getInvitationId()),
        client -> {
          InvitationDetails result = client.getInvitation(INVITATION_ID.toString());
          assertEquals(
              new InvitationDetails(
                  INVITATION_ID,
                  GROUP_ID,
                  GROUP_NAME,
                  OWNER_ID,
                  MEMBER_ID,
                  InvitationStatus.PENDING,
                  CREATED_AT,
                  Optional.empty()),
              result);
        });
  }

  /** An accepted invitation includes the upstream timestamp. */
  @Test
  void getInvitationMapsAcceptanceTimestamp() throws IOException {
    InvitationInfo response =
        invitationInfo()
            .setStatus(
                pl.kaitoudev.tabkeeper.proto.group.v1.InvitationStatus.INVITATION_STATUS_ACCEPTED)
            .setAcceptedTs(GrpcMappingUtils.toProtobufTimestamp(CREATED_AT))
            .build();
    withResponse(
        GroupServiceGrpc.getGetInvitationMethod(),
        response,
        request -> assertEquals(INVITATION_ID.toString(), request.getInvitationId()),
        client -> {
          InvitationDetails result = client.getInvitation(INVITATION_ID.toString());
          assertEquals(InvitationStatus.ACCEPTED, result.status());
          assertEquals(Optional.of(CREATED_AT), result.acceptedAt());
        });
  }

  /** Acceptance maps the invitation and group identifiers. */
  @Test
  void acceptGroupInvitationMapsAffectedIdentifiers() throws IOException {
    AcceptGroupInvitationResponse response =
        AcceptGroupInvitationResponse.newBuilder()
            .setInvitationId(INVITATION_ID.toString())
            .setGroupId(GROUP_ID.toString())
            .build();
    withResponse(
        GroupServiceGrpc.getAcceptGroupInvitationMethod(),
        response,
        request -> assertEquals(INVITATION_ID.toString(), request.getInvitationId()),
        client ->
            assertEquals(
                new InvitationAcceptedDetails(INVITATION_ID, GROUP_ID),
                client.acceptGroupInvitation(INVITATION_ID.toString())));
  }

  /** Service failures remain gRPC errors for the existing HTTP exception advice. */
  @Test
  void getGroupPropagatesUpstreamNotFound() throws IOException {
    withResponse(
        GroupServiceGrpc.getGetGroupMethod(),
        groupInfo(),
        request -> {
          throw Status.NOT_FOUND.withDescription("Group not found").asRuntimeException();
        },
        client -> {
          StatusRuntimeException exception =
              assertThrows(
                  StatusRuntimeException.class, () -> client.getGroup(GROUP_ID.toString()));
          assertEquals(Status.Code.NOT_FOUND, exception.getStatus().getCode());
        });
  }

  /** Builds a group response containing its owner membership. */
  private static GroupInfo groupInfo() {
    return GroupInfo.newBuilder()
        .setGroupId(GROUP_ID.toString())
        .setGroupName(GROUP_NAME)
        .setCreatedTs(GrpcMappingUtils.toProtobufTimestamp(CREATED_AT))
        .addMembers(
            GroupMember.newBuilder()
                .setUserId(OWNER_ID.toString())
                .setRole(pl.kaitoudev.tabkeeper.proto.group.v1.GroupRole.GROUP_ROLE_OWNER)
                .setJoinedTs(GrpcMappingUtils.toProtobufTimestamp(CREATED_AT)))
        .build();
  }

  /** Builds a pending invitation without an acceptance timestamp. */
  private static InvitationInfo.Builder invitationInfo() {
    return InvitationInfo.newBuilder()
        .setInvitationId(INVITATION_ID.toString())
        .setGroupId(GROUP_ID.toString())
        .setGroupName(GROUP_NAME)
        .setInvitedByUserId(OWNER_ID.toString())
        .setInvitedUserId(MEMBER_ID.toString())
        .setStatus(pl.kaitoudev.tabkeeper.proto.group.v1.InvitationStatus.INVITATION_STATUS_PENDING)
        .setCreatedTs(GrpcMappingUtils.toProtobufTimestamp(CREATED_AT));
  }

  /** Runs one client operation against an isolated unary service with a bounded deadline. */
  private static <RequestT, ResponseT> void withResponse(
      MethodDescriptor<RequestT, ResponseT> method,
      ResponseT response,
      Consumer<RequestT> verifyRequest,
      Consumer<GroupGrpcClient> verifyClient)
      throws IOException {
    String serverName = InProcessServerBuilder.generateName();
    ServerServiceDefinition service =
        ServerServiceDefinition.builder(GroupServiceGrpc.SERVICE_NAME)
            .addMethod(
                method,
                ServerCalls.asyncUnaryCall(
                    (request, observer) -> {
                      assertNotNull(Context.current().getDeadline());
                      assertFalse(Context.current().getDeadline().isExpired());
                      assertTrue(
                          Context.current().getDeadline().timeRemaining(TimeUnit.MILLISECONDS)
                              <= TimeUnit.SECONDS.toMillis(MAX_DEADLINE_SECONDS));
                      try {
                        verifyRequest.accept(request);
                        observer.onNext(response);
                        observer.onCompleted();
                      } catch (StatusRuntimeException exception) {
                        observer.onError(exception);
                      }
                    }))
            .build();
    Server server =
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(service)
            .build()
            .start();
    ManagedChannel channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    try {
      verifyClient.accept(new GroupGrpcClient(GroupServiceGrpc.newBlockingStub(channel)));
    } finally {
      channel.shutdownNow();
      server.shutdownNow();
    }
  }
}
