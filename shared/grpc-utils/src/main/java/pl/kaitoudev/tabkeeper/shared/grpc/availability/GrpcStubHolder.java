package pl.kaitoudev.tabkeeper.shared.grpc.availability;

import io.grpc.ManagedChannel;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class GrpcStubHolder<S> {

  private final AtomicReference<ChannelAndStub<S>> channelAndStub = new AtomicReference<>();
  private final Function<ManagedChannel, S> stubFactory;
  @Getter private final String serviceId;
  @Getter private final String unavailabilityServiceId;

  public GrpcStubHolder(
      String serviceId, Function<ManagedChannel, S> stubFactory, String unavailabilityServiceId) {
    this.serviceId = serviceId;
    this.stubFactory = stubFactory;
    this.unavailabilityServiceId = unavailabilityServiceId;
    nullify();
  }

  public S getStub() {
    ChannelAndStub<S> snapshot = this.channelAndStub.get();
    return snapshot.stub();
  }

  public void update(ManagedChannel newChannel) {
    ChannelAndStub<S> replacement = new ChannelAndStub<>(newChannel, stubFactory.apply(newChannel));

    this.channelAndStub.set(replacement);
  }

  public ChannelAndStub<S> current() {
    return this.channelAndStub.get();
  }

  public void nullify() {
    this.channelAndStub.set(nullPair());
  }

  private ChannelAndStub<S> nullPair() {
    ManagedChannel unavailableChannel =
        UnavailableManagedChannelFactory.createUnavailableManagedChannel(
            this.unavailabilityServiceId);

    return new ChannelAndStub<>(unavailableChannel, stubFactory.apply(unavailableChannel));
  }
}
