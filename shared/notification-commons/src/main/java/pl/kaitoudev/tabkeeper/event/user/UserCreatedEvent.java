package pl.kaitoudev.tabkeeper.event.user;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.kaitoudev.tabkeeper.event.AbstractEvent;
import pl.kaitoudev.tabkeeper.event.EventTypes;

/** Event published after a user is successfully created. */
@Getter
@SuperBuilder
@Jacksonized
public final class UserCreatedEvent extends AbstractEvent {
  private final UUID userId;
  private final String displayName;
  private final String email;
  private final Instant createdAt;

  /** Returns the stable user-created event type. */
  @Override
  public String getEventType() {
    return EventTypes.USER_CREATED;
  }
}
