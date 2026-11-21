package pl.kaitoudev.tabkeeper.service.expense.domain.exception;

/** Indicates that a repayment violates a financial or group-membership invariant. */
public class InvalidRepaymentException extends InvalidRequestException {

  /** Creates an exception with the supplied validation message. */
  public InvalidRepaymentException(String message) {
    super(message);
  }

  /** Creates an exception describing an invalid repayment. */
  public static InvalidRepaymentException ofMessage(String message) {
    return new InvalidRepaymentException(message);
  }
}
