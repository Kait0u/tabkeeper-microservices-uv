package pl.kaitoudev.tabkeeper.service.notification.client;

import java.util.UUID;
import pl.kaitoudev.tabkeeper.service.notification.client.model.GroupContactDetails;

/** Retrieves group information needed to personalize notifications. */
public interface GroupDirectory {

  /** Retrieves a group's display name by identifier. */
  GroupContactDetails getGroup(UUID groupId);
}
