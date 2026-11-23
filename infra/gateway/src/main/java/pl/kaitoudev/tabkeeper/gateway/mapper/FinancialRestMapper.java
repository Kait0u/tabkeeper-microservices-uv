package pl.kaitoudev.tabkeeper.gateway.mapper;

import java.util.List;
import lombok.experimental.UtilityClass;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.Allocation;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.DirectedBalanceDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.ExpenseDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.GroupBalanceDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.Monetary;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.RepaymentAllocationDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.RepaymentDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.UserExpenseHistoryDetails;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.AllocationRequest;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.AllocationResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.DirectedBalanceResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.ExpenseResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.GroupBalanceResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.MonetaryRequest;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.MonetaryResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.RepaymentAllocationResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.RepaymentResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.UserExpenseHistoryResponse;

/** Maps between financial gateway domain models and REST request/response DTOs. */
@UtilityClass
public class FinancialRestMapper {

  /** Converts REST monetary input to its domain representation. */
  public static Monetary toDomain(MonetaryRequest monetary) {
    return new Monetary(monetary.value(), monetary.currency());
  }

  /** Converts REST expense allocations to domain allocations. */
  public static List<Allocation> toDomainAllocations(List<AllocationRequest> allocations) {
    return allocations.stream().map(FinancialRestMapper::toDomain).toList();
  }

  /** Converts an expense to its REST representation. */
  public static ExpenseResponse toResponse(ExpenseDetails expense) {
    return ExpenseResponse.builder()
        .expenseId(expense.expenseId())
        .groupId(expense.groupId())
        .payingUserId(expense.payingUserId())
        .moneyAmount(toResponse(expense.moneyAmount()))
        .allocations(expense.allocations().stream().map(FinancialRestMapper::toResponse).toList())
        .createdAt(expense.createdAt())
        .build();
  }

  /** Converts a member's expense history to its REST representation. */
  public static UserExpenseHistoryResponse toResponse(UserExpenseHistoryDetails history) {
    return UserExpenseHistoryResponse.builder()
        .groupId(history.groupId())
        .userId(history.userId())
        .expensesPaid(history.expensesPaid().stream().map(FinancialRestMapper::toResponse).toList())
        .expensesAllocated(
            history.expensesAllocated().stream().map(FinancialRestMapper::toResponse).toList())
        .build();
  }

  /** Converts a group balance to its REST representation. */
  public static GroupBalanceResponse toResponse(GroupBalanceDetails groupBalance) {
    return GroupBalanceResponse.builder()
        .groupId(groupBalance.groupId())
        .balances(groupBalance.balances().stream().map(FinancialRestMapper::toResponse).toList())
        .build();
  }

  /** Converts a repayment and its allocations to their REST representation. */
  public static RepaymentResponse toResponse(RepaymentDetails repayment) {
    return RepaymentResponse.builder()
        .repaymentId(repayment.repaymentId())
        .groupId(repayment.groupId())
        .reportingUserId(repayment.reportingUserId())
        .repayingUserId(repayment.repayingUserId())
        .recipientUserId(repayment.recipientUserId())
        .moneyAmount(toResponse(repayment.moneyAmount()))
        .createdAt(repayment.createdAt())
        .allocations(repayment.allocations().stream().map(FinancialRestMapper::toResponse).toList())
        .build();
  }

  /** Converts a REST allocation to its domain representation. */
  private static Allocation toDomain(AllocationRequest allocation) {
    return new Allocation(allocation.userId(), toDomain(allocation.amount()));
  }

  /** Converts a monetary domain value to its REST representation. */
  private static MonetaryResponse toResponse(Monetary monetary) {
    return MonetaryResponse.builder().value(monetary.value()).currency(monetary.currency()).build();
  }

  /** Converts an expense allocation to its REST representation. */
  private static AllocationResponse toResponse(Allocation allocation) {
    return AllocationResponse.builder()
        .userId(allocation.userId())
        .amount(toResponse(allocation.amount()))
        .build();
  }

  /** Converts a directed balance to its REST representation. */
  private static DirectedBalanceResponse toResponse(DirectedBalanceDetails balance) {
    return DirectedBalanceResponse.builder()
        .debtorUserId(balance.debtorUserId())
        .creditorUserId(balance.creditorUserId())
        .remainingAmount(toResponse(balance.remainingAmount()))
        .build();
  }

  /** Converts a repayment allocation to its REST representation. */
  private static RepaymentAllocationResponse toResponse(RepaymentAllocationDetails allocation) {
    return RepaymentAllocationResponse.builder()
        .originExpenseId(allocation.originExpenseId())
        .appliedAmount(toResponse(allocation.appliedAmount()))
        .build();
  }
}
