package pl.kaitoudev.tabkeeper.service.expense.domain.exception;

/** Indicates that an expense violates a financial or group-membership invariant. */
public class InvalidExpenseException extends RuntimeException {

  /** Creates an exception with the supplied validation message. */
  public InvalidExpenseException(String message) {
    super(message);
  }

  /** Creates an exception describing an invalid expense. */
  public static InvalidExpenseException ofMessage(String message) {
    return new InvalidExpenseException(message);
  }
}
