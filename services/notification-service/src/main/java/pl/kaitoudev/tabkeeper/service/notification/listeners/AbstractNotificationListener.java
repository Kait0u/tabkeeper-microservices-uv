package pl.kaitoudev.tabkeeper.service.notification.listeners;

import lombok.RequiredArgsConstructor;
import pl.kaitoudev.tabkeeper.service.notification.service.NotificationService;

@RequiredArgsConstructor
public abstract class AbstractNotificationListener {
  protected final NotificationService notificationService;
}
