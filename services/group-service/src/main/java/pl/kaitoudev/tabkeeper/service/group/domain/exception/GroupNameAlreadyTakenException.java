package pl.kaitoudev.tabkeeper.service.group.domain.exception;

public class GroupNameAlreadyTakenException extends RuntimeException {

  private static final String MESSAGE_TEMPLATE = "A group by the name \"%s\" already exists.";

  private GroupNameAlreadyTakenException(String message) {
    super(message);
  }

  public static GroupNameAlreadyTakenException ofName(String name) {
    return new GroupNameAlreadyTakenException(MESSAGE_TEMPLATE.formatted(name));
  }
}
