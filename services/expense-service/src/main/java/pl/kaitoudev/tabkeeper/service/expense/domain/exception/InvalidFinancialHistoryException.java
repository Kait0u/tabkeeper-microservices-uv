package pl.kaitoudev.tabkeeper.service.expense.domain.exception;

/** Indicates that persisted financial history violates the replay algorithm's invariants. */
public class InvalidFinancialHistoryException extends RuntimeException {

  /** Creates an exception with the supplied history-invariant message. */
  public InvalidFinancialHistoryException(String message) {
    super(message);
  }

  /** Creates an exception with the supplied history-invariant message and cause. */
  public InvalidFinancialHistoryException(String message, Throwable cause) {
    super(message, cause);
  }

  /** Creates an exception describing invalid financial history. */
  public static InvalidFinancialHistoryException ofMessage(String message) {
    return new InvalidFinancialHistoryException(message);
  }

  /** Creates an exception describing invalid financial history and its cause. */
  public static InvalidFinancialHistoryException ofMessage(String message, Throwable cause) {
    return new InvalidFinancialHistoryException(message, cause);
  }
}
