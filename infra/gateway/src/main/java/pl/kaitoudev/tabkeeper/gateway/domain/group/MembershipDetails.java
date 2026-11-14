package pl.kaitoudev.tabkeeper.gateway.domain.group;

import java.util.UUID;

/** Membership and ownership results for a user in a group. */
public record MembershipDetails(UUID groupId, UUID userId, boolean isMember, boolean isOwner) {}
