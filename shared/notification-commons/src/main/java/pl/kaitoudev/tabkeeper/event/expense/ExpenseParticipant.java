package pl.kaitoudev.tabkeeper.event.expense;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/** A participant and the amount allocated to them in an expense-created event. */
@Builder
@Jacksonized
public record ExpenseParticipant(UUID userId, BigDecimal allocatedAmount) {
  public static ExpenseParticipant of(UUID userId, BigDecimal allocatedAmount) {
    return new ExpenseParticipant(userId, allocatedAmount);
  }
}
