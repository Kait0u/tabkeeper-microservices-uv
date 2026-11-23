package pl.kaitoudev.tabkeeper.gateway.domain.expense;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

/** The current directed balances for a group. */
@Builder
public record GroupBalanceDetails(UUID groupId, List<DirectedBalanceDetails> balances) {}
