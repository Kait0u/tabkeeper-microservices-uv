package pl.kaitoudev.tabkeeper.service.group.grpc;

import io.grpc.stub.StreamObserver;
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
import pl.kaitoudev.tabkeeper.service.group.domain.service.GroupService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GroupGrpcService extends GroupServiceGrpc.GroupServiceImplBase {

  private final GroupService groupService;

  @Override
  public void createGroup(CreateGroupRequest request, StreamObserver<GroupInfo> responseObserver) {
    super.createGroup(request, responseObserver);
  }

  @Override
  public void getGroup(GetGroupRequest request, StreamObserver<GroupInfo> responseObserver) {
    super.getGroup(request, responseObserver);
  }

  @Override
  public void getGroupsForUser(
      GetGroupsForUserRequest request, StreamObserver<GetGroupsForUserResponse> responseObserver) {
    super.getGroupsForUser(request, responseObserver);
  }

  @Override
  public void checkMembership(
      CheckMembershipRequest request, StreamObserver<CheckMembershipResponse> responseObserver) {
    super.checkMembership(request, responseObserver);
  }

  @Override
  public void inviteUserToGroup(
      InviteUserToGroupRequest request,
      StreamObserver<InviteUserToGroupResponse> responseObserver) {
    super.inviteUserToGroup(request, responseObserver);
  }

  @Override
  public void getInvitation(
      GetInvitationRequest request, StreamObserver<InvitationInfo> responseObserver) {
    super.getInvitation(request, responseObserver);
  }

  @Override
  public void acceptGroupInvitation(
      AcceptGroupInvitationRequest request,
      StreamObserver<AcceptGroupInvitationResponse> responseObserver) {
    super.acceptGroupInvitation(request, responseObserver);
  }
}
