package pl.kaitoudev.tabkeeper.service.notification.config.grpc;

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
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupServiceGrpc;
import pl.kaitoudev.tabkeeper.shared.grpc.availability.GrpcStubHolder;

/** Maintains a discoverable gRPC connection to Group Service. */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class GroupGrpcClientConfig {
  public static final String SERVICE_ID = "tabkeeper-group-service";
  public static final String UNAVAILABLE_SERVICE_ID = "unavailable-group-service";

  private final DiscoveryClient discoveryClient;
  private final GrpcStubHolder<GroupServiceGrpc.GroupServiceBlockingStub> holder =
      new GrpcStubHolder<>(SERVICE_ID, GroupServiceGrpc::newBlockingStub, UNAVAILABLE_SERVICE_ID);

  /** Exposes the shared Group Service stub holder to the group directory client. */
  @Bean
  public GrpcStubHolder<GroupServiceGrpc.GroupServiceBlockingStub> groupServiceStubHolder() {
    return holder;
  }

  /** Refreshes the Group Service connection when discovery supplies a new address. */
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

    ServiceInstance serviceInstance = instances.getFirst();
    String grpcPort;
    try {
      Map<String, String> metadata = Objects.requireNonNull(serviceInstance.getMetadata());
      grpcPort = Objects.requireNonNull(metadata.get("gRPC_port"));
    } catch (NullPointerException ex) {
      log.error("No gRPC port found for \"{}\".", SERVICE_ID);
      holder.nullify();
      return;
    }

    String target = serviceInstance.getHost() + ":" + grpcPort;
    if (holder.current().managedChannel().authority().equals(target)) {
      log.info("\"{}\" is already up to date.", SERVICE_ID);
      return;
    }

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(serviceInstance.getHost(), Integer.parseInt(grpcPort))
            .usePlaintext()
            .build();
    holder.update(channel);
    log.info("Successfully created a ManagedChannel for \"{}\".", SERVICE_ID);
  }
}
