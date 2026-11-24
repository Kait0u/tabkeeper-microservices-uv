package pl.kaitoudev.tabkeeper.service.notification.email;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "tabkeeper.notification-service.email")
@RequiredArgsConstructor
public class EmailConfig {
  private final String fromEmail;
}
