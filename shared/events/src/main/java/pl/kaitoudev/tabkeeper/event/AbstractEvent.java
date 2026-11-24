package pl.kaitoudev.tabkeeper.event;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@RequiredArgsConstructor
@SuperBuilder
public abstract class AbstractEvent {
  protected final UUID eventId;
  protected final String eventType;
  protected final Instant occurredAt;
}
