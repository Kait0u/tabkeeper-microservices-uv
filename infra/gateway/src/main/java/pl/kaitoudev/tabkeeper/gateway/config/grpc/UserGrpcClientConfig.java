package pl.kaitoudev.tabkeeper.gateway.config.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import pl.kaitoudev.tabkeeper.proto.user.v1.UserServiceGrpc;
import pl.kaitoudev.tabkeeper.shared.grpc.availability.GrpcStubHolder;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class UserGrpcClientConfig {

  public static final String SERVICE_ID = "tabkeeper-user-service";
  public static final String UNAVAILABLE_SERVICE_ID = "unavailable-user-service";

  private final DiscoveryClient discoveryClient;
  private final GrpcStubHolder<UserServiceGrpc.UserServiceBlockingStub> holder =
      new GrpcStubHolder<>(SERVICE_ID, UserServiceGrpc::newBlockingStub, UNAVAILABLE_SERVICE_ID);

  @Bean
  public GrpcStubHolder<UserServiceGrpc.UserServiceBlockingStub> userServiceStubHolder() {
    return holder;
  }

  @Scheduled(
      fixedDelayString = "${tabkeeper.grpc.discovery-refresh-interval:PT5S}",
      initialDelayString = "${tabkeeper.grpc.discovery-refresh-interval:PT5S}")
  public void synchronizeStub() {
    log.info("Synchronizing the stub for \"{}\".", SERVICE_ID);
    List<ServiceInstance> instances = discoveryClient.getInstances(SERVICE_ID);

    if (instances.isEmpty()) {
      log.warn("No instance found for \"{}\". Retaining the current.", SERVICE_ID);
      return;
    }

    log.debug(
        "Found {} instances for \"{}\" - picking the first one.", instances.size(), SERVICE_ID);
    ServiceInstance serviceInstance = instances.getFirst();
    String grpcPort;

    try {
      Map<String, String> metadata = Objects.requireNonNull(serviceInstance.getMetadata());
      grpcPort = Objects.requireNonNull(metadata.get("gRPC_port"));
    } catch (NullPointerException e) {
      log.error("No gRPC port found for \"{}\"", SERVICE_ID);
      holder.nullify();
      return;
    }

    String target = serviceInstance.getHost() + ":" + grpcPort;
    if (authorityHasNotChanged(target)) {
      log.info("\"{}\" is already up to date.", SERVICE_ID);
      return;
    }

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(serviceInstance.getHost(), Integer.parseInt(grpcPort))
            .usePlaintext()
            .build();
    holder.update(channel);

    log.info("Successfully created a ManagedChannel for \"{}\"", SERVICE_ID);
  }

  private boolean authorityHasNotChanged(String target) {
    return holder.current().managedChannel().authority().equals(target);
  }
}
