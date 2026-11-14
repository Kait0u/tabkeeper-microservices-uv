package pl.kaitoudev.tabkeeper.gateway.dto.group;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

/** A group summary whose ownership flag is relative to the requested user. */
@Builder
public record GroupSummaryResponse(
    UUID groupId, String groupName, boolean isOwner, Instant createdAt) {}
