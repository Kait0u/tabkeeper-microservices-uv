package pl.kaitoudev.tabkeeper.gateway.domain.group;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** An invitation with its current state and optional acceptance timestamp. */
public record InvitationDetails(
    UUID invitationId,
    UUID groupId,
    String groupName,
    UUID invitedByUserId,
    UUID invitedUserId,
    InvitationStatus status,
    Instant createdAt,
    Optional<Instant> acceptedAt) {}
