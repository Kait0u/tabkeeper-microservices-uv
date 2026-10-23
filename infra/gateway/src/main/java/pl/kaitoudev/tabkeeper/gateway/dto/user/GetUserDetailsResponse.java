package pl.kaitoudev.tabkeeper.gateway.dto.user;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GetUserDetailsResponse(
    UUID userId, String displayName, String email, Instant createdAt) {}
