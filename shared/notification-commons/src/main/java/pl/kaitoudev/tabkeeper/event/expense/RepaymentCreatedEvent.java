package pl.kaitoudev.tabkeeper.event.expense;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.kaitoudev.tabkeeper.event.AbstractEvent;
import pl.kaitoudev.tabkeeper.event.EventTypes;

/** Event published after a repayment between group members is recorded. */
@Getter
@SuperBuilder
@Jacksonized
public final class RepaymentCreatedEvent extends AbstractEvent {
  private final UUID repaymentId;
  private final UUID groupId;
  private final UUID reportingUserId;
  private final UUID repayingUserId;
  private final UUID recipientUserId;
  private final BigDecimal amount;
  private final String currency;
  private final Instant createdAt;

  /** Returns the stable repayment-created event type. */
  @Override
  public String getEventType() {
    return EventTypes.REPAYMENT_CREATED;
  }
}
