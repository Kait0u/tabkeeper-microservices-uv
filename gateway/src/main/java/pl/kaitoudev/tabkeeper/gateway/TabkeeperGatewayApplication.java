package pl.kaitoudev.tabkeeper.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
public class TabkeeperGatewayApplication {

  @RequestMapping("/")
  public String dummyIndex() {
    return "Hello World!";
  }

  public static void main(String[] args) {
    SpringApplication.run(TabkeeperGatewayApplication.class, args);
  }
}
