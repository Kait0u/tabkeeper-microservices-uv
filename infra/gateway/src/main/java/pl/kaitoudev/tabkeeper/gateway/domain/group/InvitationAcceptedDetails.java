package pl.kaitoudev.tabkeeper.gateway.domain.group;

import java.util.UUID;

/** Identifiers returned when an invitation is accepted. */
public record InvitationAcceptedDetails(UUID invitationId, UUID groupId) {}
