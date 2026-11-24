package pl.kaitoudev.tabkeeper.service.notification.email.renderer;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.event.user.UserCreatedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserTemplateRenderer {
  private final GenericTemplateRenderer genericTemplateRenderer;

  /** Renders a welcome email for the user who has just joined the platform. */
  public String renderUserCreatedEmail(UserCreatedEvent userCreatedEvent) {
    log.info("Rendering email for UserCreatedEvent (EventID={})", userCreatedEvent.getEventId());

    Map<String, Object> variables =
        Map.ofEntries(Map.entry("displayName", userCreatedEvent.getDisplayName()));

    return genericTemplateRenderer.render("mail/user-created", variables);
  }
}
