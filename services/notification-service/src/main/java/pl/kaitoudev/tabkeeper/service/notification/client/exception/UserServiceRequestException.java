package pl.kaitoudev.tabkeeper.service.notification.client.exception;

import java.util.UUID;

/** Indicates that User Service rejected or failed a request for an unexpected reason. */
public class UserServiceRequestException extends RuntimeException {

  /** Creates an exception with the supplied dependency-failure message and cause. */
  public UserServiceRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  /** Creates an exception for an unsuccessful request concerning the supplied user. */
  public static UserServiceRequestException ofUser(UUID userId, Throwable cause) {
    return new UserServiceRequestException(
        "User Service failed to retrieve user %s.".formatted(userId), cause);
  }
}
