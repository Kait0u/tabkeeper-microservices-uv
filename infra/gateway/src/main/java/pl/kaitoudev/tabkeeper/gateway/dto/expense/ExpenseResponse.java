package pl.kaitoudev.tabkeeper.gateway.dto.expense;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/** Expense details returned by creation, retrieval, and history endpoints. */
@Builder
public record ExpenseResponse(
    UUID expenseId,
    UUID groupId,
    UUID payingUserId,
    MonetaryResponse moneyAmount,
    List<AllocationResponse> allocations,
    Instant createdAt) {}
