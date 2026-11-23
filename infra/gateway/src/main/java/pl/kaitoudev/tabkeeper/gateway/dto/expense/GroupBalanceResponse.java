package pl.kaitoudev.tabkeeper.gateway.dto.expense;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

/** Current balances for all debtor-creditor pairs in a group. */
@Builder
public record GroupBalanceResponse(UUID groupId, List<DirectedBalanceResponse> balances) {}
