package pl.kaitoudev.tabkeeper.service.group.domain.exception;

import java.util.UUID;

public class NonexistentGroupException extends RuntimeException {

  private static final String MESSAGE_TEMPLATE = "A group by the ID \"%s\" does not exist.";

  private NonexistentGroupException(String message) {
    super(message);
  }

  public static NonexistentGroupException ofId(UUID groupId) {
    return new NonexistentGroupException(MESSAGE_TEMPLATE.formatted(groupId));
  }
}
