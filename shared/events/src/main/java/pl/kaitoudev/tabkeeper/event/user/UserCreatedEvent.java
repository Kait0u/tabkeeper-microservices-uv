package pl.kaitoudev.tabkeeper.event.user;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import pl.kaitoudev.tabkeeper.event.AbstractEvent;

/** Event published after a user is successfully created. */
@Getter
@SuperBuilder
public final class UserCreatedEvent extends AbstractEvent {
  private final UUID userId;
  private final String displayName;
  private final String email;
  private final Instant createdAt;
}
