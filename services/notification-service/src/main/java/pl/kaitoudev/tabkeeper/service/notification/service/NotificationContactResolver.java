package pl.kaitoudev.tabkeeper.service.notification.service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.service.notification.client.GroupDirectory;
import pl.kaitoudev.tabkeeper.service.notification.client.UserDirectory;
import pl.kaitoudev.tabkeeper.service.notification.client.model.UserContactDetails;

/** Resolves group and user contact information needed by notification listeners. */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationContactResolver {
  private final GroupDirectory groupDirectory;
  private final UserDirectory userDirectory;

  /** Resolves a group's display name once. */
  public String resolveGroupName(UUID groupId) {
    log.info("Establishing the identity of Group {}.", groupId);
    return groupDirectory.getGroup(groupId).groupName();
  }

  /** Resolves one user's display name and email address. */
  public UserContactDetails resolveUser(UUID userId) {
    log.info("Establishing the identity of User {}.", userId);
    return userDirectory.getUser(userId);
  }

  /** Resolves each distinct user ID once while preserving the supplied ID order. */
  public Map<UUID, UserContactDetails> resolveUsers(Collection<UUID> userIds) {
    log.info("Establishing user identities for {} user(s)...", userIds.size());
    Map<UUID, UserContactDetails> contacts = new LinkedHashMap<>();
    userIds.stream()
        .distinct()
        .forEach(
            userId -> {
              contacts.put(userId, userDirectory.getUser(userId));
              log.info("Established the identity of User {}.", userId);
            });

    return Collections.unmodifiableMap(contacts);
  }
}
