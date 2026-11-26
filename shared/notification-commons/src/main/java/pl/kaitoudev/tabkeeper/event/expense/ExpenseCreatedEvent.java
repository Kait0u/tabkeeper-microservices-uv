package pl.kaitoudev.tabkeeper.event.expense;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.kaitoudev.tabkeeper.event.AbstractEvent;
import pl.kaitoudev.tabkeeper.event.EventTypes;

/** Event published after an expense and its participant allocations are created. */
@Getter
@SuperBuilder
@Jacksonized
public final class ExpenseCreatedEvent extends AbstractEvent {
  private final UUID expenseId;
  private final UUID groupId;
  private final UUID payingUserId;
  private final BigDecimal amount;
  private final String currency;
  private final List<ExpenseParticipant> participants;
  private final Instant createdAt;

  /** Returns the stable expense-created event type. */
  @Override
  public String getEventType() {
    return EventTypes.EXPENSE_CREATED;
  }
}
