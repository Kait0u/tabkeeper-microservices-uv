package pl.kaitoudev.tabkeeper.service.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TabkeeperNotificationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(TabkeeperNotificationServiceApplication.class, args);
  }
}
