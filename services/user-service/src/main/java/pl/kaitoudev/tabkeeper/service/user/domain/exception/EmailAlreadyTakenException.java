package pl.kaitoudev.tabkeeper.service.user.domain.exception;

public class EmailAlreadyTakenException extends RuntimeException {

  public EmailAlreadyTakenException(String email) {
    super("Email already taken: " + email);
  }
}
