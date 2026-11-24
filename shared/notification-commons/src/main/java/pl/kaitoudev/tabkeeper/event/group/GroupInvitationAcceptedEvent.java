package pl.kaitoudev.tabkeeper.event.group;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import pl.kaitoudev.tabkeeper.event.AbstractEvent;

/** Event published after a group invitation is accepted. */
@Getter
@SuperBuilder
public final class GroupInvitationAcceptedEvent extends AbstractEvent {
  private final UUID invitationId;
  private final UUID groupId;
  private final String groupName;
  private final UUID invitedByUserId;
  private final UUID invitedUserId;
  private final Instant acceptedAt;
}
