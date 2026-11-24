package pl.kaitoudev.tabkeeper.service.notification.service;

public interface NotificationService {
  void sendNotification(String recipient, String subject, String messageText);
}
