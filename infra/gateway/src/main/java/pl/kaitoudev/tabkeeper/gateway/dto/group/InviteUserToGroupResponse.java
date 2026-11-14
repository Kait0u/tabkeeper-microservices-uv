package pl.kaitoudev.tabkeeper.gateway.dto.group;

import java.util.UUID;
import lombok.Builder;

/** Identifiers returned after creating a group invitation. */
@Builder
public record InviteUserToGroupResponse(
    UUID invitationId, UUID groupId, UUID invitedByUserId, UUID invitedUserId) {}
