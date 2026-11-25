package pl.kaitoudev.tabkeeper.config;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Shared JSON and publisher configuration for notification-producing services. */
@Configuration
public class NotificationMessagingConfig {
  public static final String EXCHANGE_NAME = "tabkeeper.events";

  /** Converts notification events to JSON with Java time support. */
  @Bean
  JacksonJsonMessageConverter notificationMessageConverter() {
    return new JacksonJsonMessageConverter();
  }
}
