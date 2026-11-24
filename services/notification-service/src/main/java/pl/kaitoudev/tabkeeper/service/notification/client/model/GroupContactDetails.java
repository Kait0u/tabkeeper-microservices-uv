package pl.kaitoudev.tabkeeper.service.notification.client.model;

import java.util.UUID;
import lombok.Builder;

/** Group information returned to notification consumers. */
@Builder
public record GroupContactDetails(UUID groupId, String groupName) {}
