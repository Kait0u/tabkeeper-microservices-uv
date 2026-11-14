package pl.kaitoudev.tabkeeper.gateway.config.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.kaitoudev.tabkeeper.gateway.util.grpc.UnavailableManagedChannelFactory;
import pl.kaitoudev.tabkeeper.proto.user.v1.UserServiceGrpc;

@Configuration
@Slf4j
public class UserGrpcClientConfig {

  public static final String SERVICE_ID = "tabkeeper-user-service";
  public static final String UNAVAILABLE_SERVICE_ID = "unavailable-user-service";

  @Bean
  public ManagedChannel userServiceChannel(DiscoveryClient discoveryClient) {
    List<ServiceInstance> instances = discoveryClient.getInstances(SERVICE_ID);

    if (instances.isEmpty()) {
      log.warn("No instance found for \"{}\"", SERVICE_ID);
      return UnavailableManagedChannelFactory.createUnavailableManagedChannel(
          UNAVAILABLE_SERVICE_ID);
    } else {
      log.debug(
          "Found {} instances for \"{}\" - picking the first one.", instances.size(), SERVICE_ID);
    }

    ServiceInstance serviceInstance = instances.getFirst();

    String grpcPort;

    try {
      Map<String, String> metadata = Objects.requireNonNull(serviceInstance.getMetadata());
      grpcPort = Objects.requireNonNull(metadata.get("gRPC_port"));
    } catch (NullPointerException e) {
      log.error("No gRPC port found for \"{}\"", SERVICE_ID);
      return UnavailableManagedChannelFactory.createUnavailableManagedChannel(
          UNAVAILABLE_SERVICE_ID);
    }

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(serviceInstance.getHost(), Integer.parseInt(grpcPort))
            .usePlaintext()
            .build();

    log.info("Successfully created a ManagedChannel for \"{}\"", SERVICE_ID);

    return channel;
  }

  @Bean
  public UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub(
      @Qualifier("userServiceChannel") ManagedChannel userServiceChannel) {
    return UserServiceGrpc.newBlockingStub(userServiceChannel);
  }
}
