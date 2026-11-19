package pl.kaitoudev.tabkeeper.service.expense.client.exception;

public abstract class GroupServiceException extends RuntimeException {

  public GroupServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
