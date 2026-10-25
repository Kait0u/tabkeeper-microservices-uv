package pl.kaitoudev.tabkeeper.service.group.domain.exception;

import java.util.UUID;

public class NonexistentInvitationException extends RuntimeException {

  private static final String MESSAGE_TEMPLATE = "An invitation by the ID \"%s\" does not exist.";

  private NonexistentInvitationException(String message) {
    super(message);
  }

  public static NonexistentInvitationException ofId(UUID invitationId) {
    return new NonexistentInvitationException(MESSAGE_TEMPLATE.formatted(invitationId));
  }
}
