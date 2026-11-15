package pl.kaitoudev.tabkeeper.gateway.dto.group;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/** Group details returned by the REST API. */
@Builder
public record GetGroupDetailsResponse(
    UUID groupId, String groupName, String currencyCode, List<GroupMemberResponse> members, Instant createdAt) {}
