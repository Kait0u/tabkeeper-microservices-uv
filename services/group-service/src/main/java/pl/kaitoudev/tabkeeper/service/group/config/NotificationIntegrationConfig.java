package pl.kaitoudev.tabkeeper.service.group.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pl.kaitoudev.tabkeeper.config.NotificationMessagingConfig;

@Configuration
@Import(NotificationMessagingConfig.class)
public class NotificationIntegrationConfig {}