package pl.kaitoudev.tabkeeper.gateway.domain.user;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserDetails(UUID userId, String displayName, String email, Instant createdAt) {}
