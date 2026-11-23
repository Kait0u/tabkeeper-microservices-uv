package pl.kaitoudev.tabkeeper.gateway.dto.expense;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

/** A member's expense history separated by their financial role. */
@Builder
public record UserExpenseHistoryResponse(
    UUID groupId,
    UUID userId,
    List<ExpenseResponse> expensesPaid,
    List<ExpenseResponse> expensesAllocated) {}
