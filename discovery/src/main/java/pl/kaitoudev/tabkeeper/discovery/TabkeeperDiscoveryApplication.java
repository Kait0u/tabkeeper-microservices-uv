package pl.kaitoudev.tabkeeper.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class TabkeeperDiscoveryApplication {

  public static void main(String[] args) {
    SpringApplication.run(TabkeeperDiscoveryApplication.class, args);
  }
}
