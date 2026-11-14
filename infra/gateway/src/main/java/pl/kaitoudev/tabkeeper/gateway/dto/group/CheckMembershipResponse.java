package pl.kaitoudev.tabkeeper.gateway.dto.group;

import java.util.UUID;
import lombok.Builder;

/** Membership information only; these flags do not authenticate the caller. */
@Builder
public record CheckMembershipResponse(
    UUID groupId, UUID userId, boolean isMember, boolean isOwner) {}
