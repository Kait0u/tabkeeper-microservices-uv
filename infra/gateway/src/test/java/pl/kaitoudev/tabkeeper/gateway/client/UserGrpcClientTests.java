package pl.kaitoudev.tabkeeper.gateway.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import pl.kaitoudev.tabkeeper.gateway.domain.user.UserDetails;
import pl.kaitoudev.tabkeeper.proto.user.v1.UserInfo;
import pl.kaitoudev.tabkeeper.proto.user.v1.UserServiceGrpc;
import pl.kaitoudev.tabkeeper.shared.grpc.GrpcMappingUtils;

/** Exercises user-client request mapping, deadlines, and error propagation over in-process gRPC. */
@Slf4j
class UserGrpcClientTests {
  /** Identifier returned by the service and sent in lookup requests. */
  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  /** Display name used in registration requests and service responses. */
  private static final String DISPLAY_NAME = "Alice";

  /** Email used in registration requests and service responses. */
  private static final String EMAIL = "alice@example.com";

  /** Test-only password whose forwarding is verified without logging its contents. */
  private static final String PASSWORD = "test-only-password";

  /** Timestamp fixture verifies preservation of nanosecond precision. */
  private static final Instant CREATED_AT = Instant.parse("2026-11-14T12:00:00.123456789Z");

  /** Expected deadline applied by user lookups. */
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(3);

  /** Expected deadline applied by user creation. */
  private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(5);

  /** Retrieval sends the requested identifier and maps every user response field. */
  @Test
  void getUserSendsIdentifierAndMapsUserDetails() throws IOException {
    withService(
        UserServiceGrpc.getGetUserMethod(),
        READ_TIMEOUT,
        observer -> {
          observer.onNext(userInfo());
          observer.onCompleted();
        },
        request -> assertEquals(USER_ID.toString(), request.getUserId()),
        client -> assertEquals(expectedUserDetails(), client.getUser(USER_ID.toString())));
  }

  /** Creation forwards all registration fields and maps the assigned identifier and timestamp. */
  @Test
  void createUserSendsRegistrationFieldsAndMapsCreatedUser() throws IOException {
    withService(
        UserServiceGrpc.getCreateUserMethod(),
        WRITE_TIMEOUT,
        observer -> {
          observer.onNext(userInfo());
          observer.onCompleted();
        },
        request -> {
          assertEquals(DISPLAY_NAME, request.getDisplayName());
          assertEquals(EMAIL, request.getEmail());
          assertEquals(PASSWORD, request.getPassword());
        },
        client ->
            assertEquals(expectedUserDetails(), client.createUser(DISPLAY_NAME, EMAIL, PASSWORD)));
  }

  /** Missing users remain NOT_FOUND errors for the gateway's existing exception advice. */
  @Test
  void getUserPropagatesNotFound() throws IOException {
    withService(
        UserServiceGrpc.getGetUserMethod(),
        READ_TIMEOUT,
        observer -> observer.onError(Status.NOT_FOUND.asRuntimeException()),
        request -> assertEquals(USER_ID.toString(), request.getUserId()),
        client -> {
          StatusRuntimeException exception =
              assertThrows(StatusRuntimeException.class, () -> client.getUser(USER_ID.toString()));
          assertEquals(Status.Code.NOT_FOUND, exception.getStatus().getCode());
        });
  }

  /** Duplicate registrations retain the upstream ALREADY_EXISTS status. */
  @Test
  void createUserPropagatesAlreadyExists() throws IOException {
    withService(
        UserServiceGrpc.getCreateUserMethod(),
        WRITE_TIMEOUT,
        observer -> observer.onError(Status.ALREADY_EXISTS.asRuntimeException()),
        request -> assertEquals(EMAIL, request.getEmail()),
        client -> {
          StatusRuntimeException exception =
              assertThrows(
                  StatusRuntimeException.class,
                  () -> client.createUser(DISPLAY_NAME, EMAIL, PASSWORD));
          assertEquals(Status.Code.ALREADY_EXISTS, exception.getStatus().getCode());
        });
  }

  /** Service unavailability is propagated rather than replaced with fabricated user data. */
  @Test
  void getUserPropagatesUnavailable() throws IOException {
    withService(
        UserServiceGrpc.getGetUserMethod(),
        READ_TIMEOUT,
        observer -> observer.onError(Status.UNAVAILABLE.asRuntimeException()),
        request -> assertEquals(USER_ID.toString(), request.getUserId()),
        client -> {
          StatusRuntimeException exception =
              assertThrows(StatusRuntimeException.class, () -> client.getUser(USER_ID.toString()));
          assertEquals(Status.Code.UNAVAILABLE, exception.getStatus().getCode());
        });
  }

  /** Upstream deadline errors reach the existing HTTP gateway-timeout translation unchanged. */
  @Test
  void createUserPropagatesDeadlineExceeded() throws IOException {
    withService(
        UserServiceGrpc.getCreateUserMethod(),
        WRITE_TIMEOUT,
        observer -> observer.onError(Status.DEADLINE_EXCEEDED.asRuntimeException()),
        request -> assertEquals(EMAIL, request.getEmail()),
        client -> {
          StatusRuntimeException exception =
              assertThrows(
                  StatusRuntimeException.class,
                  () -> client.createUser(DISPLAY_NAME, EMAIL, PASSWORD));
          assertEquals(Status.Code.DEADLINE_EXCEEDED, exception.getStatus().getCode());
        });
  }

  /** Builds a complete upstream response for successful calls. */
  private static UserInfo userInfo() {
    return UserInfo.newBuilder()
        .setUserId(USER_ID.toString())
        .setDisplayName(DISPLAY_NAME)
        .setEmail(EMAIL)
        .setCreatedTs(GrpcMappingUtils.toProtobufTimestamp(CREATED_AT))
        .build();
  }

  /** Builds the gateway representation expected from successful calls. */
  private static UserDetails expectedUserDetails() {
    return UserDetails.builder()
        .userId(USER_ID)
        .displayName(DISPLAY_NAME)
        .email(EMAIL)
        .createdAt(CREATED_AT)
        .build();
  }

  /**
   * Runs one client call against an isolated service, asserting captured requests on the test
   * thread. Deadline checks inspect propagation without introducing sleeps or waiting for actual
   * expiry.
   */
  private static <RequestT, ResponseT> void withService(
      MethodDescriptor<RequestT, ResponseT> method,
      Duration expectedTimeout,
      Consumer<StreamObserver<ResponseT>> respond,
      Consumer<RequestT> verifyRequest,
      Consumer<UserGrpcClient> verifyClient)
      throws IOException {
    String serverName = InProcessServerBuilder.generateName();
    AtomicReference<RequestT> capturedRequest = new AtomicReference<>();
    AtomicReference<Long> remainingDeadlineNanos = new AtomicReference<>();
    ServerServiceDefinition service =
        ServerServiceDefinition.builder(UserServiceGrpc.SERVICE_NAME)
            .addMethod(
                method,
                ServerCalls.asyncUnaryCall(
                    (request, observer) -> {
                      capturedRequest.set(request);
                      if (Context.current().getDeadline() != null) {
                        remainingDeadlineNanos.set(
                            Context.current().getDeadline().timeRemaining(TimeUnit.NANOSECONDS));
                      }
                      respond.accept(observer);
                    }))
            .build();
    Server server =
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(service)
            .build()
            .start();
    ManagedChannel channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    log.info("Testing user gRPC method {}", method.getFullMethodName());
    try {
      verifyClient.accept(new UserGrpcClient(UserServiceGrpc.newBlockingStub(channel)));
      assertNotNull(capturedRequest.get(), "The service must receive the client request");
      verifyRequest.accept(capturedRequest.get());
      Long remainingNanos = remainingDeadlineNanos.get();
      assertNotNull(remainingNanos, "The client must propagate a deadline");
      assertTrue(remainingNanos > 0, "The request must arrive before its deadline");
      assertTrue(remainingNanos <= expectedTimeout.toNanos(), "The deadline must be bounded");
      log.info(
          "Verified request, response or error, and deadline for {}", method.getFullMethodName());
    } finally {
      channel.shutdownNow();
      server.shutdownNow();
    }
  }
}
