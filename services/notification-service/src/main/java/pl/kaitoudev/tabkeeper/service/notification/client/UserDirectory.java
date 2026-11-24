package pl.kaitoudev.tabkeeper.service.notification.client;

import java.util.UUID;
import pl.kaitoudev.tabkeeper.service.notification.client.model.UserContactDetails;

/** Retrieves user contact information needed to personalize notifications. */
public interface UserDirectory {

  /** Retrieves a user's display name and email address by identifier. */
  UserContactDetails getUser(UUID userId);
}
