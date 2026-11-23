package pl.kaitoudev.tabkeeper.gateway.domain.expense;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

/** A member's expense history separated into paid and allocated expenses. */
@Builder
public record UserExpenseHistoryDetails(
    UUID groupId,
    UUID userId,
    List<ExpenseDetails> expensesPaid,
    List<ExpenseDetails> expensesAllocated) {}
