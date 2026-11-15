package pl.kaitoudev.tabkeeper.service.group.domain.exception;

public class InvalidCurrencyCodeException extends RuntimeException {

  private static final String MESSAGE_TEMPLATE =
      "Currency code \"%s\" is not valid. Should be exactly 3 uppercase letters.";

  public InvalidCurrencyCodeException(String message) {
    super(message);
  }

  public static InvalidCurrencyCodeException ofCode(String currencyCode) {
    return new InvalidCurrencyCodeException(MESSAGE_TEMPLATE.formatted(currencyCode));
  }
}
