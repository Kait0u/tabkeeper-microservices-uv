package pl.kaitoudev.tabkeeper.gateway.dto.group;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import org.jspecify.annotations.Nullable;
import pl.kaitoudev.tabkeeper.gateway.domain.group.InvitationStatus;

/** Invitation details; the acceptance timestamp is null until supplied by the service. */
@Builder
public record GetInvitationDetailsResponse(
    UUID invitationId,
    UUID groupId,
    String groupName,
    UUID invitedByUserId,
    UUID invitedUserId,
    InvitationStatus status,
    Instant createdAt,
    @Nullable Instant acceptedAt) {}
