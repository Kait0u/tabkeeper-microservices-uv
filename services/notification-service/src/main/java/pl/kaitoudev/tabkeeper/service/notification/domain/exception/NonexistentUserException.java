package pl.kaitoudev.tabkeeper.service.notification.domain.exception;

import java.util.UUID;

/** Indicates that the requested user does not exist. */
public class NonexistentUserException extends RuntimeException {

  /** Creates an exception with the supplied user-not-found message. */
  public NonexistentUserException(String message) {
    super(message);
  }

  /** Creates an exception identifying the missing user. */
  public static NonexistentUserException ofId(UUID userId) {
    return new NonexistentUserException("User %s not found.".formatted(userId));
  }
}
