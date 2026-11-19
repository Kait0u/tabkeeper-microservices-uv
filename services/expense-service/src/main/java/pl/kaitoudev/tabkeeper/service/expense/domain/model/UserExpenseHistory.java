package pl.kaitoudev.tabkeeper.service.expense.domain.model;

import java.util.List;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.Expense;

/**
 * Expense history for a group member, separated by the member's role in each expense.
 *
 * @param paidExpenses expenses paid by the member
 * @param allocatedExpenses expenses containing a share allocated to the member
 */
public record UserExpenseHistory(List<Expense> paidExpenses, List<Expense> allocatedExpenses) {}
