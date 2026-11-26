package pl.kaitoudev.tabkeeper.event.group;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.kaitoudev.tabkeeper.event.AbstractEvent;
import pl.kaitoudev.tabkeeper.event.EventTypes;

/** Event published after a user is invited to a group. */
@Getter
@SuperBuilder
@Jacksonized
public final class GroupInvitationCreatedEvent extends AbstractEvent {
  private final UUID invitationId;
  private final UUID groupId;
  private final String groupName;
  private final UUID invitedByUserId;
  private final UUID invitedUserId;
  private final Instant createdAt;

  /** Returns the stable group-invitation-created event type. */
  @Override
  public String getEventType() {
    return EventTypes.GROUPINVITE_CREATED;
  }
}
