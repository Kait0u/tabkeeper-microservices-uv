package pl.kaitoudev.tabkeeper.gateway.util.grpc;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class UnavailableManagedChannelFactory {

  public ManagedChannel createUnavailableManagedChannel(String name) {
    ManagedChannel channel = InProcessChannelBuilder.forName(name).directExecutor().build();

    log.info("Created unavailable channel \"{}\"", name);

    return channel;
  }
}
