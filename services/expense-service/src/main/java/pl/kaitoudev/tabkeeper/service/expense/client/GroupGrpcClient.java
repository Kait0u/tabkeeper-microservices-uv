package pl.kaitoudev.tabkeeper.service.expense.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.proto.group.v1.GetGroupRequest;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupInfo;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupServiceGrpc;
import pl.kaitoudev.tabkeeper.service.expense.client.exception.GroupServiceRequestException;
import pl.kaitoudev.tabkeeper.service.expense.client.exception.GroupServiceUnavailableException;
import pl.kaitoudev.tabkeeper.service.expense.domain.exception.NonexistentGroupException;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.GroupDetails;
import pl.kaitoudev.tabkeeper.shared.grpc.availability.GrpcStubHolder;

/** Retrieves the current group details needed to validate expense-service operations. */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupGrpcClient {
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(3);

  private final GrpcStubHolder<GroupServiceGrpc.GroupServiceBlockingStub> holder;

  /**
   * Retrieves a group's currency and membership through Group Service.
   *
   * <p>Transport failures are translated into this client's exception types so that callers never
   * depend on Group Service's gRPC error representation.
   */
  public GroupDetails getGroup(UUID groupId) {
    GetGroupRequest request = GetGroupRequest.newBuilder().setGroupId(groupId.toString()).build();
    GroupInfo response;
    try {
      response = holder.getStub().withDeadlineAfter(READ_TIMEOUT).getGroup(request);
    } catch (StatusRuntimeException ex) {
      throw translateFailure(groupId, ex);
    }

    try {
      Set<UUID> memberIds =
          response.getMembersList().stream()
              .map(member -> UUID.fromString(member.getUserId()))
              .collect(Collectors.toUnmodifiableSet());
      return new GroupDetails(
          UUID.fromString(response.getGroupId()), response.getCurrencyCode(), memberIds);
    } catch (IllegalArgumentException ex) {
      throw GroupServiceRequestException.ofGroup(groupId, ex);
    }
  }

  /** Converts a Group Service transport failure into an expense-service client exception. */
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
