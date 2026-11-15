package pl.kaitoudev.tabkeeper.gateway.dto.group;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/** Details of a newly created group, including its owner membership. */
@Builder
public record CreateGroupResponse(
    UUID groupId,
    String groupName,
    String currencyCode,
    List<GroupMemberResponse> members,
    Instant createdAt) {}
