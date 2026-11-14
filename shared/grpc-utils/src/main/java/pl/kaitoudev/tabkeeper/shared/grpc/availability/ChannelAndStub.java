package pl.kaitoudev.tabkeeper.shared.grpc.availability;

import io.grpc.ManagedChannel;

public record ChannelAndStub<S>(ManagedChannel managedChannel, S stub) {}
