package pl.kaitoudev.tabkeeper.service.expense.client;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.proto.group.v1.GetGroupRequest;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupInfo;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupServiceGrpc;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.GroupDetails;
import pl.kaitoudev.tabkeeper.shared.grpc.availability.GrpcStubHolder;

/** Retrieves the current group details needed to validate expense-service operations. */
@Component
@RequiredArgsConstructor
public class GroupGrpcClient {
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(3);

  private final GrpcStubHolder<GroupServiceGrpc.GroupServiceBlockingStub> holder;

  /**
   * Retrieves a group's currency and membership through Group Service.
   *
   * <p>Upstream gRPC statuses, including NOT_FOUND and UNAVAILABLE, retain their original status
   * when handled by the expense service's gRPC advice.
   */
  public GroupDetails getGroup(UUID groupId) {
    GetGroupRequest request = GetGroupRequest.newBuilder().setGroupId(groupId.toString()).build();
    GroupInfo response = holder.getStub().withDeadlineAfter(READ_TIMEOUT).getGroup(request);

    Set<UUID> memberIds =
        response.getMembersList().stream()
            .map(member -> UUID.fromString(member.getUserId()))
            .collect(Collectors.toUnmodifiableSet());
    return new GroupDetails(
        UUID.fromString(response.getGroupId()), response.getCurrencyCode(), memberIds);
  }
}
