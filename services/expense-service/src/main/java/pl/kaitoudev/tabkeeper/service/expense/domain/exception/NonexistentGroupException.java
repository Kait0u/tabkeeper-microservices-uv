package pl.kaitoudev.tabkeeper.service.expense.domain.exception;

import java.util.UUID;

/** Indicates that the requested group does not exist. */
public class NonexistentGroupException extends RuntimeException {

  /** Creates an exception with the supplied group-not-found message. */
  public NonexistentGroupException(String message) {
    super(message);
  }

  /** Creates an exception identifying the missing group. */
  public static NonexistentGroupException ofId(UUID groupId) {
    return new NonexistentGroupException("Group %s not found.".formatted(groupId));
  }
}
