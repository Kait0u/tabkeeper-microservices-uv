package pl.kaitoudev.tabkeeper.service.notification.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.event.user.UserCreatedEvent;
import pl.kaitoudev.tabkeeper.service.notification.email.NotificationTitles;
import pl.kaitoudev.tabkeeper.service.notification.email.renderer.UserTemplateRenderer;
import pl.kaitoudev.tabkeeper.service.notification.service.NotificationService;

@Slf4j
@Component
public class UserNotificationListener extends AbstractNotificationListener {
  private final UserTemplateRenderer userTemplateRenderer;

  public UserNotificationListener(
      NotificationService notificationService, UserTemplateRenderer userTemplateRenderer) {
    super(notificationService);
    this.userTemplateRenderer = userTemplateRenderer;
  }

  @RabbitListener(queues = "user.created")
  public void handleUserCreated(UserCreatedEvent event) {
    log.info("Received UserCreated event {}", event.getEventId());

    String htmlBody = userTemplateRenderer.renderUserCreatedEmail(event);
    log.info("Sending email to user {} - {}...", event.getUserId(), event.getEmail());
    notificationService.sendNotification(
        event.getEmail(), NotificationTitles.USER_CREATED, htmlBody);

    log.info("UserCreated event {} handled.", event.getEventId());
  }
}
