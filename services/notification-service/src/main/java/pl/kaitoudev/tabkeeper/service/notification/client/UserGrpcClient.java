package pl.kaitoudev.tabkeeper.service.notification.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.proto.user.v1.GetUserRequest;
import pl.kaitoudev.tabkeeper.proto.user.v1.UserInfo;
import pl.kaitoudev.tabkeeper.proto.user.v1.UserServiceGrpc;
import pl.kaitoudev.tabkeeper.service.notification.client.exception.UserServiceRequestException;
import pl.kaitoudev.tabkeeper.service.notification.client.exception.UserServiceUnavailableException;
import pl.kaitoudev.tabkeeper.service.notification.client.model.UserContactDetails;
import pl.kaitoudev.tabkeeper.service.notification.domain.exception.NonexistentUserException;
import pl.kaitoudev.tabkeeper.shared.grpc.availability.GrpcStubHolder;

/** Retrieves user contact details through User Service's discovery-backed gRPC API. */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserGrpcClient implements UserDirectory {
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(3);

  private final GrpcStubHolder<UserServiceGrpc.UserServiceBlockingStub> holder;

  /**
   * Retrieves a user's display name and email address.
   *
   * <p>Transport and response-mapping failures are translated into notification-specific exceptions
   * so callers never depend on User Service's gRPC error representation.
   */
  @Override
  public UserContactDetails getUser(UUID userId) {
    GetUserRequest request = GetUserRequest.newBuilder().setUserId(userId.toString()).build();
    UserInfo response;
    try {
      response = holder.getStub().withDeadlineAfter(READ_TIMEOUT).getUser(request);
    } catch (StatusRuntimeException ex) {
      throw translateFailure(userId, ex);
    }

    try {
      return UserContactDetails.builder()
          .userId(UUID.fromString(response.getUserId()))
          .displayName(response.getDisplayName())
          .email(response.getEmail())
          .build();
    } catch (IllegalArgumentException ex) {
      throw UserServiceRequestException.ofUser(userId, ex);
    }
  }

  /** Converts a User Service transport failure into a notification-service exception. */
  private static RuntimeException translateFailure(UUID userId, StatusRuntimeException exception) {
    Status.Code code = exception.getStatus().getCode();
    if (code == Status.Code.NOT_FOUND) {
      return NonexistentUserException.ofId(userId);
    }
    if (code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED) {
      log.warn("User Service is unavailable for user {}: {}.", userId, code);
      return UserServiceUnavailableException.ofUser(userId, exception);
    }

    log.error("User Service request for user {} failed with status {}.", userId, code, exception);
    return UserServiceRequestException.ofUser(userId, exception);
  }
}
