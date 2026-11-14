package pl.kaitoudev.tabkeeper.gateway.domain.group;

import java.time.Instant;
import java.util.UUID;

/** A group summary with ownership relative to the requested user. */
public record GroupSummaryDetails(
    UUID groupId, String groupName, boolean isOwner, Instant createdAt) {}
