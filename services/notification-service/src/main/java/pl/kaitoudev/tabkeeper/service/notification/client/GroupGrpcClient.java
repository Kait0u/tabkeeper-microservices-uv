package pl.kaitoudev.tabkeeper.service.notification.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.proto.group.v1.GetGroupRequest;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupInfo;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupServiceGrpc;
import pl.kaitoudev.tabkeeper.service.notification.client.exception.GroupServiceRequestException;
import pl.kaitoudev.tabkeeper.service.notification.client.exception.GroupServiceUnavailableException;
import pl.kaitoudev.tabkeeper.service.notification.client.model.GroupContactDetails;
import pl.kaitoudev.tabkeeper.service.notification.domain.exception.NonexistentGroupException;
import pl.kaitoudev.tabkeeper.shared.grpc.availability.GrpcStubHolder;

/** Retrieves group details through Group Service's discovery-backed gRPC API. */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupGrpcClient implements GroupDirectory {
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(3);

  private final GrpcStubHolder<GroupServiceGrpc.GroupServiceBlockingStub> holder;

  /**
   * Retrieves a group's display name.
   *
   * <p>Transport and response-mapping failures are translated into notification-specific exceptions
   * so callers never depend on Group Service's gRPC error representation.
   */
  @Override
  public GroupContactDetails getGroup(UUID groupId) {
    GetGroupRequest request = GetGroupRequest.newBuilder().setGroupId(groupId.toString()).build();
    GroupInfo response;
    try {
      response = holder.getStub().withDeadlineAfter(READ_TIMEOUT).getGroup(request);
    } catch (StatusRuntimeException ex) {
      throw translateFailure(groupId, ex);
    }

    try {
      return GroupContactDetails.builder()
          .groupId(UUID.fromString(response.getGroupId()))
          .groupName(response.getGroupName())
          .build();
    } catch (IllegalArgumentException ex) {
      throw GroupServiceRequestException.ofGroup(groupId, ex);
    }
  }

  /** Converts a Group Service transport failure into a notification-service exception. */
  private static RuntimeException translateFailure(UUID groupId, StatusRuntimeException exception) {
    Status.Code code = exception.getStatus().getCode();
    if (code == Status.Code.NOT_FOUND) {
      return NonexistentGroupException.ofId(groupId);
    }
    if (code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED) {
      log.warn("Group Service is unavailable for group {}: {}.", groupId, code);
      return GroupServiceUnavailableException.ofGroup(groupId, exception);
    }

    log.error(
        "Group Service request for group {} failed with status {}.", groupId, code, exception);
    return GroupServiceRequestException.ofGroup(groupId, exception);
  }
}
