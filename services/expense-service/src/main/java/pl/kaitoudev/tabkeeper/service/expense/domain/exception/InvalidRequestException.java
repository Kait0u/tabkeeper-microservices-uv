package pl.kaitoudev.tabkeeper.service.expense.domain.exception;

/** Indicates that a request violates an expense-service input or domain invariant. */
public class InvalidRequestException extends RuntimeException {

  /** Creates an exception with the supplied validation message. */
  protected InvalidRequestException(String message) {
    super(message);
  }

  /** Creates an exception with the supplied validation message and parsing cause. */
  protected InvalidRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  /** Creates an exception describing an invalid request. */
  public static InvalidRequestException ofMessage(String message) {
    return new InvalidRequestException(message);
  }

  /** Creates an exception describing invalid request data and its parsing cause. */
  public static InvalidRequestException ofMessage(String message, Throwable cause) {
    return new InvalidRequestException(message, cause);
  }
}
