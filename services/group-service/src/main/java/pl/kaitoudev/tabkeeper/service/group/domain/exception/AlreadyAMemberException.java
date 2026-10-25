package pl.kaitoudev.tabkeeper.service.group.domain.exception;

import java.util.UUID;

public class AlreadyAMemberException extends RuntimeException {

  private static final String MESSAGE_TEMPLATE =
      "User with ID=%s is already a member of group with ID=%s.";

  private AlreadyAMemberException(String message) {
    super(message);
  }

  public static AlreadyAMemberException ofId(UUID groupId, UUID userId) {
    return new AlreadyAMemberException(MESSAGE_TEMPLATE.formatted(userId, groupId));
  }
}
