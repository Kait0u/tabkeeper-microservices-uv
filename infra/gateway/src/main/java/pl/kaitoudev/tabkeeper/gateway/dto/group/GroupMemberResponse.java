package pl.kaitoudev.tabkeeper.gateway.dto.group;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import pl.kaitoudev.tabkeeper.gateway.domain.group.GroupRole;

/** A member included in a REST group response. */
@Builder
public record GroupMemberResponse(UUID userId, GroupRole role, Instant joinedAt) {}
