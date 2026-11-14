package pl.kaitoudev.tabkeeper.gateway.domain.group;

import java.time.Instant;
import java.util.UUID;

/** A user's membership in a group. */
public record GroupMemberDetails(UUID userId, GroupRole role, Instant joinedAt) {}
