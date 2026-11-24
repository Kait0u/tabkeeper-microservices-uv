package pl.kaitoudev.tabkeeper.service.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables scheduled synchronization with service discovery. */
@Configuration
@EnableScheduling
public class SchedulingConfig {}
