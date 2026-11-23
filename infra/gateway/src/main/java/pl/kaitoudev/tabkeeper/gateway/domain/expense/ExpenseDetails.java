package pl.kaitoudev.tabkeeper.gateway.domain.expense;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/** An expense and its participant allocations, independent of its gRPC representation. */
@Builder
public record ExpenseDetails(
    UUID expenseId,
    UUID groupId,
    UUID payingUserId,
    Monetary moneyAmount,
    List<Allocation> allocations,
    Instant createdAt) {}
