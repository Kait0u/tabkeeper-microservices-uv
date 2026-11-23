package pl.kaitoudev.tabkeeper.gateway.dto.expense;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/** Expense creation input; user IDs are supplied without authentication. */
public record CreateExpenseRequest(
    @NotNull UUID groupId,
    @NotNull UUID payingUserId,
    @Valid @NotNull MonetaryRequest moneyAmount,
    @NotEmpty List<@Valid @NotNull AllocationRequest> allocations) {}
