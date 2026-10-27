package pl.kaitoudev.tabkeeper.service.group.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
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
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupServiceGrpc;
import pl.kaitoudev.tabkeeper.proto.group.v1.InvitationInfo;
import pl.kaitoudev.tabkeeper.proto.group.v1.InviteUserToGroupRequest;
import pl.kaitoudev.tabkeeper.proto.group.v1.InviteUserToGroupResponse;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.Group;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.GroupInvitation;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.AlreadyAMemberException;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.AlreadyInvitedException;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.GroupNameAlreadyTakenException;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.NonexistentGroupException;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.NonexistentInvitationException;
import pl.kaitoudev.tabkeeper.service.group.domain.mapper.GroupInfoMapper;
import pl.kaitoudev.tabkeeper.service.group.domain.mapper.GroupMemberMapper;
import pl.kaitoudev.tabkeeper.service.group.domain.mapper.InvitationStatusMapper;
import pl.kaitoudev.tabkeeper.service.group.domain.service.GroupInvitationService;
import pl.kaitoudev.tabkeeper.service.group.domain.service.GroupService;
import pl.kaitoudev.tabkeeper.shared.grpc.GrpcMappingUtils;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GroupGrpcService extends GroupServiceGrpc.GroupServiceImplBase {

  private final GroupService groupService;
  private final GroupInvitationService groupInvitationService;

  @Override
  public void createGroup(CreateGroupRequest request, StreamObserver<GroupInfo> responseObserver) {
    Group newGroup;

    try {
      newGroup =
          groupService.createGroup(request.getGroupName(), UUID.fromString(request.getOwnerId()));
    } catch (GroupNameAlreadyTakenException ex) {
      responseObserver.onError(
          Status.ALREADY_EXISTS
              .withCause(ex)
              .withDescription(ex.getMessage())
              .asRuntimeException());
      return;
    }

    GroupInfo groupInfo =
        GroupInfo.newBuilder()
            .setGroupId(newGroup.getId().toString())
            .setGroupName(newGroup.getName())
            .addAllMembers(
                newGroup.getGroupMemberships().stream().map(GroupMemberMapper::toProtobuf).toList())
            .setCreatedTs(GrpcMappingUtils.toProtobufTimestamp(newGroup.getCreatedTimestamp()))
            .build();

    responseObserver.onNext(groupInfo);
    responseObserver.onCompleted();
  }

  @Override
  public void getGroup(GetGroupRequest request, StreamObserver<GroupInfo> responseObserver) {
    Optional<Group> optGroup =
        groupService.getGroupByIdWithMembers(UUID.fromString(request.getGroupId()));

    if (optGroup.isEmpty()) {
      responseObserver.onError(
          Status.NOT_FOUND
              .withDescription("Group %s not found.".formatted(request.getGroupId()))
              .asRuntimeException());
      return;
    }

    Group group = optGroup.get();

    GroupInfo groupInfo =
        GroupInfo.newBuilder()
            .setGroupId(group.getId().toString())
            .setGroupName(group.getName())
            .addAllMembers(
                group.getGroupMemberships().stream().map(GroupMemberMapper::toProtobuf).toList())
            .setCreatedTs(GrpcMappingUtils.toProtobufTimestamp(group.getCreatedTimestamp()))
            .build();

    responseObserver.onNext(groupInfo);
    responseObserver.onCompleted();
  }

  @Override
  public void getGroupsForUser(
      GetGroupsForUserRequest request, StreamObserver<GetGroupsForUserResponse> responseObserver) {

    List<Group> groupsForUser = groupService.getGroupsForUser(UUID.fromString(request.getUserId()));

    Set<Group> groupsOwnedByUser =
        new HashSet<>(groupService.getOwnedGroupsForUser(UUID.fromString(request.getUserId())));

    Map<Group, Boolean> isOwnedByUserMap = new HashMap<>();

    for (Group group : groupsForUser) {
      isOwnedByUserMap.put(group, groupsOwnedByUser.contains(group));
    }

    Predicate<Group> ownershipPredicate = isOwnedByUserMap::get;

    GetGroupsForUserResponse response =
        GetGroupsForUserResponse.newBuilder()
            .setUserId(request.getUserId())
            .addAllGroupsOfUser(
                groupsForUser.stream()
                    .map(group -> GroupInfoMapper.groupToGroupInfo(group, ownershipPredicate))
                    .toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void checkMembership(
      CheckMembershipRequest request, StreamObserver<CheckMembershipResponse> responseObserver) {
    boolean isOwner =
        groupService.checkOwnership(
            UUID.fromString(request.getGroupId()), UUID.fromString(request.getUserId()));

    boolean isMember =
        isOwner
            || groupService.checkMembership(
                UUID.fromString(request.getGroupId()), UUID.fromString(request.getUserId()));

    CheckMembershipResponse response =
        CheckMembershipResponse.newBuilder()
            .setGroupId(request.getGroupId())
            .setUserId(request.getUserId())
            .setIsMember(isMember)
            .setIsOwner(isOwner)
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void inviteUserToGroup(
      InviteUserToGroupRequest request,
      StreamObserver<InviteUserToGroupResponse> responseObserver) {

    GroupInvitation groupInvitation;
    try {
      groupInvitation =
          groupInvitationService.inviteUserToGroup(
              UUID.fromString(request.getGroupId()),
              UUID.fromString(request.getInvitedByUserId()),
              UUID.fromString(request.getInvitedUserId()));
    } catch (NonexistentGroupException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (AlreadyInvitedException | AlreadyAMemberException ex) {
      responseObserver.onError(
          Status.ALREADY_EXISTS
              .withCause(ex)
              .withDescription(ex.getMessage())
              .asRuntimeException());
      return;
    }

    InviteUserToGroupResponse response =
        InviteUserToGroupResponse.newBuilder()
            .setInvitationId(groupInvitation.getId().toString())
            .setGroupId(groupInvitation.getGroup().getId().toString())
            .setInvitedByUserId(groupInvitation.getInvitedBy().toString())
            .setInvitedUserId(groupInvitation.getInvitedUser().toString())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getInvitation(
      GetInvitationRequest request, StreamObserver<InvitationInfo> responseObserver) {
    GroupInvitation groupInvitation;

    try {
      groupInvitation =
          groupInvitationService.getGroupInvitationWithGroup(
              UUID.fromString(request.getInvitationId()));
    } catch (NonexistentInvitationException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    }

    InvitationInfo.Builder responseBuilder =
        InvitationInfo.newBuilder()
            .setInvitationId(groupInvitation.getId().toString())
            .setGroupId(groupInvitation.getGroup().getId().toString())
            .setGroupName(groupInvitation.getGroup().getName())
            .setInvitedByUserId(groupInvitation.getInvitedBy().toString())
            .setInvitedUserId(groupInvitation.getInvitedUser().toString())
            .setCreatedTs(
                GrpcMappingUtils.toProtobufTimestamp(groupInvitation.getCreatedTimestamp()))
            .setStatus(InvitationStatusMapper.toProtobuf(groupInvitation.getStatus()));

    if (Objects.nonNull(groupInvitation.getRespondedTimestamp())) {
      responseBuilder.setAcceptedTs(
          GrpcMappingUtils.toProtobufTimestamp(groupInvitation.getRespondedTimestamp()));
    }

    InvitationInfo response = responseBuilder.build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void acceptGroupInvitation(
      AcceptGroupInvitationRequest request,
      StreamObserver<AcceptGroupInvitationResponse> responseObserver) {

    GroupInvitation groupInvitation;
    try {
      groupInvitation =
          groupInvitationService.acceptInvitation(UUID.fromString(request.getInvitationId()));
    } catch (NonexistentInvitationException | NonexistentGroupException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    }

    AcceptGroupInvitationResponse response =
        AcceptGroupInvitationResponse.newBuilder()
            .setInvitationId(groupInvitation.getId().toString())
            .setGroupId(groupInvitation.getGroup().getId().toString())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
