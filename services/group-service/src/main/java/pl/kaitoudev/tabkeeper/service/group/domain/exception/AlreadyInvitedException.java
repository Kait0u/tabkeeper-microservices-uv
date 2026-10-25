package pl.kaitoudev.tabkeeper.service.group.domain.exception;

import java.util.UUID;

public class AlreadyInvitedException extends RuntimeException {

  private static final String MESSAGE_TEMPLATE =
      "User with ID=%s is already invited to group with ID=%s.";

  private AlreadyInvitedException(String message) {
    super(message);
  }

  public static AlreadyInvitedException ofId(UUID groupId, UUID userId) {
    return new AlreadyInvitedException(MESSAGE_TEMPLATE.formatted(userId, groupId));
  }
}
