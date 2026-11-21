package pl.kaitoudev.tabkeeper.service.expense.domain.exception;

import java.util.UUID;

/** Indicates that a group's local coordination record is missing after initialization. */
public class MissingExpenseGroupException extends RuntimeException {

  /** Creates an exception with the supplied invariant-violation message. */
  public MissingExpenseGroupException(String message) {
    super(message);
  }

  /** Creates an exception identifying the missing local group. */
  public static MissingExpenseGroupException ofGroup(UUID groupId) {
    return new MissingExpenseGroupException(
        "Local expense group %s is missing after initialization.".formatted(groupId));
  }
}
