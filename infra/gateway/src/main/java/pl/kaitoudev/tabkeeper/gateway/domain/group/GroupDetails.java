package pl.kaitoudev.tabkeeper.gateway.domain.group;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/** A group's details, independent of its gRPC representation. */
@Builder
public record GroupDetails(
    UUID groupId,
    String groupName,
    String currencyCode,
    List<GroupMemberDetails>
    members,
    Instant createdAt
) {}
