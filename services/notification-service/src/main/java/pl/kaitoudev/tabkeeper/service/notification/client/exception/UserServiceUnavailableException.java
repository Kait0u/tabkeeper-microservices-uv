package pl.kaitoudev.tabkeeper.service.notification.client.exception;

import java.util.UUID;

/** Indicates that User Service could not be reached or did not respond before the deadline. */
public class UserServiceUnavailableException extends RuntimeException {

  /** Creates an exception with the supplied dependency-failure message and cause. */
  public UserServiceUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }

  /** Creates an exception for a failed attempt to retrieve the supplied user. */
  public static UserServiceUnavailableException ofUser(UUID userId, Throwable cause) {
    return new UserServiceUnavailableException(
        "User Service is unavailable while retrieving user %s.".formatted(userId), cause);
  }
}
