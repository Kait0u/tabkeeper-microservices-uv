package pl.kaitoudev.tabkeeper.gateway.dto.group;

import java.util.UUID;
import lombok.Builder;

/** Identifiers returned after accepting a group invitation. */
@Builder
public record AcceptGroupInvitationResponse(UUID invitationId, UUID groupId) {}
