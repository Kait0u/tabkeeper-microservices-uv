package pl.kaitoudev.tabkeeper.service.notification.client.model;

import java.util.UUID;
import lombok.Builder;

/** Contact details returned to notification consumers for a user. */
@Builder
public record UserContactDetails(UUID userId, String displayName, String email) {}
