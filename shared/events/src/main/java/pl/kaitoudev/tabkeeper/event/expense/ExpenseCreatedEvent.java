package pl.kaitoudev.tabkeeper.event.expense;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import pl.kaitoudev.tabkeeper.event.AbstractEvent;

/** Event published after an expense and its participant allocations are created. */
@Getter
@SuperBuilder
public final class ExpenseCreatedEvent extends AbstractEvent {
  private final UUID expenseId;
  private final UUID groupId;
  private final UUID payingUserId;
  private final BigDecimal amount;
  private final String currency;
  private final List<ExpenseParticipant> participants;
  private final Instant createdAt;
}
