package pl.kaitoudev.tabkeeper.gateway.client;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.gateway.domain.group.GroupDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.GroupMemberDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.GroupRole;
import pl.kaitoudev.tabkeeper.gateway.domain.group.GroupSummaryDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.InvitationAcceptedDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.InvitationCreatedDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.InvitationDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.InvitationStatus;
import pl.kaitoudev.tabkeeper.gateway.domain.group.MembershipDetails;
import pl.kaitoudev.tabkeeper.proto.group.v1.AcceptGroupInvitationRequest;
import pl.kaitoudev.tabkeeper.proto.group.v1.AcceptGroupInvitationResponse;
import pl.kaitoudev.tabkeeper.proto.group.v1.CheckMembershipRequest;
import pl.kaitoudev.tabkeeper.proto.group.v1.CheckMembershipResponse;
import pl.kaitoudev.tabkeeper.proto.group.v1.CreateGroupRequest;
import pl.kaitoudev.tabkeeper.proto.group.v1.GetGroupRequest;
import pl.kaitoudev.tabkeeper.proto.group.v1.GetGroupsForUserRequest;
import pl.kaitoudev.tabkeeper.proto.group.v1.GetGroupsForUserResponse;
import pl.kaitoudev.tabkeeper.proto.group.v1.GetInvitationRequest;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupInfo;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupMember;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupServiceGrpc;
import pl.kaitoudev.tabkeeper.proto.group.v1.InvitationInfo;
import pl.kaitoudev.tabkeeper.proto.group.v1.InviteUserToGroupRequest;
import pl.kaitoudev.tabkeeper.proto.group.v1.InviteUserToGroupResponse;
import pl.kaitoudev.tabkeeper.shared.grpc.GrpcMappingUtils;
import pl.kaitoudev.tabkeeper.shared.grpc.availability.GrpcStubHolder;

/** Calls the Group Service and maps its protobuf responses to gateway domain models. */
@Component
@RequiredArgsConstructor
public class GroupGrpcClient {
  /** Deadline for read-only calls, matching the user client. */
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(3);

  /** Deadline for commands, matching the user client. */
  private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(5);

  private final GrpcStubHolder<GroupServiceGrpc.GroupServiceBlockingStub> holder;

  /** Retrieves a group and its members; upstream gRPC errors propagate to gateway advice. */
  public GroupDetails getGroup(String groupId) {
    GetGroupRequest request = GetGroupRequest.newBuilder().setGroupId(groupId).build();

    GroupInfo response = holder.getStub().withDeadlineAfter(READ_TIMEOUT).getGroup(request);

    return toGroupDetails(response);
  }

  /** Creates a group with the supplied user as owner. */
  public GroupDetails createGroup(String ownerId, String groupName) {
    CreateGroupRequest request =
        CreateGroupRequest.newBuilder().setOwnerId(ownerId).setGroupName(groupName).build();

    GroupInfo response = holder.getStub().withDeadlineAfter(WRITE_TIMEOUT).createGroup(request);

    return toGroupDetails(response);
  }

  /** Lists groups for a user, preserving the service's ordering and ownership flags. */
  public List<GroupSummaryDetails> getGroupsForUser(String userId) {
    GetGroupsForUserRequest request =
        GetGroupsForUserRequest.newBuilder().setUserId(userId).build();

    GetGroupsForUserResponse response =
        holder.getStub().withDeadlineAfter(READ_TIMEOUT).getGroupsForUser(request);

    return response.getGroupsOfUserList().stream()
        .map(
            group ->
                new GroupSummaryDetails(
                    UUID.fromString(group.getGroupId()),
                    group.getGroupName(),
                    group.getIsOwner(),
                    GrpcMappingUtils.fromProtobufTimestamp(group.getCreatedTs())))
        .toList();
  }

  /** Checks a user's membership and ownership without applying authorization policy. */
  public MembershipDetails checkMembership(String groupId, String userId) {
    CheckMembershipRequest request =
        CheckMembershipRequest.newBuilder().setGroupId(groupId).setUserId(userId).build();

    CheckMembershipResponse response =
        holder.getStub().withDeadlineAfter(READ_TIMEOUT).checkMembership(request);

    return new MembershipDetails(
        UUID.fromString(response.getGroupId()),
        UUID.fromString(response.getUserId()),
        response.getIsMember(),
        response.getIsOwner());
  }

  /** Creates an invitation on behalf of the supplied inviter. */
  public InvitationCreatedDetails inviteUserToGroup(
      String groupId, String invitedByUserId, String invitedUserId) {
    InviteUserToGroupRequest request =
        InviteUserToGroupRequest.newBuilder()
            .setGroupId(groupId)
            .setInvitedByUserId(invitedByUserId)
            .setInvitedUserId(invitedUserId)
            .build();

    InviteUserToGroupResponse response =
        holder.getStub().withDeadlineAfter(WRITE_TIMEOUT).inviteUserToGroup(request);

    return new InvitationCreatedDetails(
        UUID.fromString(response.getInvitationId()),
        UUID.fromString(response.getGroupId()),
        UUID.fromString(response.getInvitedByUserId()),
        UUID.fromString(response.getInvitedUserId()));
  }

  /** Retrieves an invitation, preserving absence of its optional acceptance timestamp. */
  public InvitationDetails getInvitation(String invitationId) {
    GetInvitationRequest request =
        GetInvitationRequest.newBuilder().setInvitationId(invitationId).build();

    InvitationInfo response =
        holder.getStub().withDeadlineAfter(READ_TIMEOUT).getInvitation(request);

    return new InvitationDetails(
        UUID.fromString(response.getInvitationId()),
        UUID.fromString(response.getGroupId()),
        response.getGroupName(),
        UUID.fromString(response.getInvitedByUserId()),
        UUID.fromString(response.getInvitedUserId()),
        toInvitationStatus(response.getStatus()),
        GrpcMappingUtils.fromProtobufTimestamp(response.getCreatedTs()),
        response.hasAcceptedTs()
            ? Optional.of(GrpcMappingUtils.fromProtobufTimestamp(response.getAcceptedTs()))
            : Optional.empty());
  }

  /** Accepts an invitation and returns the affected invitation and group identifiers. */
  public InvitationAcceptedDetails acceptGroupInvitation(String invitationId) {
    AcceptGroupInvitationRequest request =
        AcceptGroupInvitationRequest.newBuilder().setInvitationId(invitationId).build();

    AcceptGroupInvitationResponse response =
        holder.getStub().withDeadlineAfter(WRITE_TIMEOUT).acceptGroupInvitation(request);

    return new InvitationAcceptedDetails(
        UUID.fromString(response.getInvitationId()), UUID.fromString(response.getGroupId()));
  }

  /** Maps the common response used by group creation and retrieval. */
  private static GroupDetails toGroupDetails(GroupInfo response) {
    return GroupDetails.builder()
        .groupId(UUID.fromString(response.getGroupId()))
        .groupName(response.getGroupName())
        .members(response.getMembersList().stream().map(GroupGrpcClient::toMemberDetails).toList())
        .createdAt(GrpcMappingUtils.fromProtobufTimestamp(response.getCreatedTs()))
        .build();
  }

  /** Maps a member without leaking protobuf types into the gateway domain. */
  private static GroupMemberDetails toMemberDetails(GroupMember member) {
    GroupRole role =
        switch (member.getRole()) {
          case GROUP_ROLE_UNSPECIFIED -> GroupRole.UNSPECIFIED;
          case GROUP_ROLE_OWNER -> GroupRole.OWNER;
          case GROUP_ROLE_MEMBER -> GroupRole.MEMBER;
          case UNRECOGNIZED -> GroupRole.UNRECOGNIZED;
        };

    return new GroupMemberDetails(
        UUID.fromString(member.getUserId()),
        role,
        GrpcMappingUtils.fromProtobufTimestamp(member.getJoinedTs()));
  }

  /** Maps invitation states while retaining unknown values for forward compatibility. */
  private static InvitationStatus toInvitationStatus(
      pl.kaitoudev.tabkeeper.proto.group.v1.InvitationStatus status) {
    return switch (status) {
      case INVITATION_STATUS_UNSPECIFIED -> InvitationStatus.UNSPECIFIED;
      case INVITATION_STATUS_PENDING -> InvitationStatus.PENDING;
      case INVITATION_STATUS_ACCEPTED -> InvitationStatus.ACCEPTED;
      case INVITATION_STATUS_DECLINED -> InvitationStatus.DECLINED;
      case INVITATION_STATUS_REVOKED -> InvitationStatus.REVOKED;
      case UNRECOGNIZED -> InvitationStatus.UNRECOGNIZED;
    };
  }
}
