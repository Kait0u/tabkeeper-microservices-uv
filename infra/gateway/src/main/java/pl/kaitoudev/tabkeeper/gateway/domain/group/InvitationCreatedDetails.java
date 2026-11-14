package pl.kaitoudev.tabkeeper.gateway.domain.group;

import java.util.UUID;

/** Identifiers returned when an invitation is created. */
public record InvitationCreatedDetails(
    UUID invitationId, UUID groupId, UUID invitedByUserId, UUID invitedUserId) {}
