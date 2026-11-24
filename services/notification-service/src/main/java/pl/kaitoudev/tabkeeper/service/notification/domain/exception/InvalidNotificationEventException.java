package pl.kaitoudev.tabkeeper.service.notification.domain.exception;

/** Indicates that an event lacks data required to render a notification correctly. */
public class InvalidNotificationEventException extends RuntimeException {

  /** Creates an exception with the supplied event-invariant message. */
  public InvalidNotificationEventException(String message) {
    super(message);
  }

  /** Creates an exception describing missing or inconsistent event data. */
  public static InvalidNotificationEventException ofMessage(String message) {
    return new InvalidNotificationEventException(message);
  }
}
