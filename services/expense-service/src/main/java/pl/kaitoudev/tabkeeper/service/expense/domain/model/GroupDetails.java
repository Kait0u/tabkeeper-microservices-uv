package pl.kaitoudev.tabkeeper.service.expense.domain.model;

import java.util.Set;
import java.util.UUID;

/**
 * A snapshot of the group attributes needed to validate financial transactions.
 *
 * @param groupId identifier of the group
 * @param currency currency configured by Group Service
 * @param memberIds identifiers of the group's current members
 */
public record GroupDetails(UUID groupId, String currency, Set<UUID> memberIds) {
  public boolean isMember(UUID userId) {
    return memberIds.contains(userId);
  }
}
