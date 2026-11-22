package pl.kaitoudev.tabkeeper.service.expense.domain.service.balance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.Expense;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.ExpenseAllocation;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.Repayment;
import pl.kaitoudev.tabkeeper.service.expense.domain.exception.InvalidFinancialHistoryException;
import pl.kaitoudev.tabkeeper.service.expense.domain.exception.InvalidRepaymentException;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.DebtFragment;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.DebtKey;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.DirectedDebt;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.GroupBalanceResult;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.RepaymentAllocation;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.RepaymentDetails;

/** Replays a group's financial history to calculate its current directed balances. */
@Component
@RequiredArgsConstructor
public class GroupBalanceCalculator {

  /** Calculates current balances without retaining replay state between requests. */
  public GroupBalanceResult calculate(
      List<Expense> expenses, List<Repayment> repayments, String currency) {
    Map<DebtKey, List<DebtFragment>> debts = new HashMap<>();

    replayExistingHistory(expenses, repayments, debts);

    return summarizeRemainingDebts(debts, currency);
  }

  /** Replays existing history and applies a candidate repayment, returning its FIFO allocations. */
  public List<RepaymentAllocation> calculateRepayment(
      List<Expense> expenses, List<Repayment> repayments, Repayment candidate) {
    Map<DebtKey, List<DebtFragment>> debts = new HashMap<>();
    replayExistingHistory(expenses, repayments, debts);

    return applyRepayment(candidate, debts);
  }

  /** Replays history and reconstructs the FIFO allocations of every persisted repayment. */
  public List<RepaymentDetails> calculateRepaymentHistory(
      List<Expense> expenses, List<Repayment> repayments) {
    Map<DebtKey, List<DebtFragment>> debts = new HashMap<>();
    Map<UUID, List<RepaymentAllocation>> allocations =
        replayExistingHistory(expenses, repayments, debts);

    return repayments.stream()
        .sorted(Comparator.comparingLong(Repayment::getSequence))
        .map(
            repayment ->
                RepaymentDetails.builder()
                    .repayment(repayment)
                    .allocations(allocations.getOrDefault(repayment.getId(), List.of()))
                    .build())
        .toList();
  }

  /** Replays history and returns allocations keyed by persisted repayment ID. */
  private Map<UUID, List<RepaymentAllocation>> replayExistingHistory(
      List<Expense> expenses, List<Repayment> repayments, Map<DebtKey, List<DebtFragment>> debts) {
    Map<UUID, List<RepaymentAllocation>> allocations = new HashMap<>();
    try {
      replayInSequenceOrder(expenses, repayments, debts, allocations);
    } catch (InvalidRepaymentException ex) {
      throw InvalidFinancialHistoryException.ofMessage(
          "Stored repayment history contains an overpayment.", ex);
    }
    return allocations;
  }

  /** Applies each transaction exactly once, following the group-local shared sequence. */
  private void replayInSequenceOrder(
      List<Expense> expenses,
      List<Repayment> repayments,
      Map<DebtKey, List<DebtFragment>> debts,
      Map<UUID, List<RepaymentAllocation>> allocations) {
    List<Expense> orderedExpenses =
        expenses.stream().sorted(Comparator.comparingLong(Expense::getSequence)).toList();
    List<Repayment> orderedRepayments =
        repayments.stream().sorted(Comparator.comparingLong(Repayment::getSequence)).toList();

    int expenseIndex = 0;
    int repaymentIndex = 0;
    // Merge both histories so each repayment is applied at its historical position.
    while (expenseIndex < orderedExpenses.size() || repaymentIndex < orderedRepayments.size()) {
      if (expenseIndex >= orderedExpenses.size()) {
        recordRepayment(orderedRepayments.get(repaymentIndex), debts, allocations);
        ++repaymentIndex;
        continue;
      }
      if (repaymentIndex >= orderedRepayments.size()) {
        applyExpense(orderedExpenses.get(expenseIndex), debts);
        ++expenseIndex;
        continue;
      }

      Expense nextExpense = orderedExpenses.get(expenseIndex);
      Repayment nextRepayment = orderedRepayments.get(repaymentIndex);
      if (nextExpense.getSequence().equals(nextRepayment.getSequence())) {
        throw InvalidFinancialHistoryException.ofMessage(
            "Expense and repayment sequences must be distinct within a group.");
      }

      if (nextExpense.getSequence() < nextRepayment.getSequence()) {
        applyExpense(nextExpense, debts);
        ++expenseIndex;
      } else {
        recordRepayment(nextRepayment, debts, allocations);
        ++repaymentIndex;
      }
    }
  }

  /** Applies a persisted repayment and retains its allocations for history responses. */
  private void recordRepayment(
      Repayment repayment,
      Map<DebtKey, List<DebtFragment>> debts,
      Map<UUID, List<RepaymentAllocation>> allocations) {
    allocations.put(repayment.getId(), applyRepayment(repayment, debts));
  }

  /** Applies participant shares, offsetting reverse obligations before creating new debt. */
  private void applyExpense(Expense expense, Map<DebtKey, List<DebtFragment>> debts) {
    for (ExpenseAllocation allocation : expense.getAllocations()) {
      // The payer's own share and zero shares do not create obligations.
      if (allocation.getUserId().equals(expense.getPayingUserId())
          || allocation.getAmount().signum() == 0) {
        continue;
      }

      DebtKey newDebtKey =
          DebtKey.builder()
              .debtorId(allocation.getUserId())
              .creditorId(expense.getPayingUserId())
              .build();
      DebtKey reverseDebtKey =
          DebtKey.builder()
              .debtorId(expense.getPayingUserId())
              .creditorId(allocation.getUserId())
              .build();

      // First cancel what the payer already owes this participant.
      BigDecimal remainingAmount =
          consumeOldest(reverseDebtKey, allocation.getAmount(), debts).remainingAmount();

      if (remainingAmount.signum() > 0) {
        // Only the remainder becomes a new participant-to-payer debt.
        DebtFragment newFragment =
            DebtFragment.builder()
                .originExpenseId(expense.getId())
                .originSequence(expense.getSequence())
                .remainingAmount(remainingAmount)
                .build();
        debts.computeIfAbsent(newDebtKey, key -> new ArrayList<>()).add(newFragment);
      }
    }
  }

  /** Applies a repayment FIFO and returns the expense-derived allocations it consumed. */
  private List<RepaymentAllocation> applyRepayment(
      Repayment repayment, Map<DebtKey, List<DebtFragment>> debts) {
    DebtKey repaymentDebtKey =
        DebtKey.builder()
            .debtorId(repayment.getRepayingUserId())
            .creditorId(repayment.getRecipientUserId())
            .build();

    // Repayments reduce only this direction and never create reverse credit.
    ConsumptionResult consumption = consumeOldest(repaymentDebtKey, repayment.getAmount(), debts);

    if (consumption.remainingAmount().signum() > 0) {
      throw InvalidRepaymentException.ofMessage(
          "Repayment from user %s to user %s exceeds the outstanding debt by %s."
              .formatted(
                  repayment.getRepayingUserId(),
                  repayment.getRecipientUserId(),
                  consumption.remainingAmount().toPlainString()));
    }

    return consumption.consumedFragments().stream()
        .map(
            fragment ->
                RepaymentAllocation.builder()
                    .originExpenseId(fragment.originExpenseId())
                    .appliedAmount(fragment.appliedAmount())
                    .build())
        .toList();
  }

  /**
   * Consumes debt fragments in FIFO order and records the origin and amount of each consumption.
   */
  private ConsumptionResult consumeOldest(
      DebtKey debtKey, BigDecimal amount, Map<DebtKey, List<DebtFragment>> debts) {
    List<DebtFragment> fragments = debts.get(debtKey);
    if (fragments == null) {
      return new ConsumptionResult(amount, List.of());
    }

    BigDecimal remainingAmount = amount;
    List<ConsumedFragment> consumedFragments = new ArrayList<>();
    Iterator<DebtFragment> fragmentIterator = fragments.iterator();
    // Fragments are appended in sequence order, so iteration settles the oldest debts first.
    while (fragmentIterator.hasNext() && remainingAmount.signum() > 0) {
      DebtFragment fragment = fragmentIterator.next();
      BigDecimal consumedAmount = fragment.consume(remainingAmount);
      remainingAmount = remainingAmount.subtract(consumedAmount);
      consumedFragments.add(new ConsumedFragment(fragment.getOriginExpenseId(), consumedAmount));

      if (fragment.isSettled()) {
        fragmentIterator.remove();
      }
    }

    if (fragments.isEmpty()) {
      debts.remove(debtKey);
    }

    return new ConsumptionResult(remainingAmount, consumedFragments);
  }

  /** Aggregates remaining fragments by direction and returns deterministic balance ordering. */
  private GroupBalanceResult summarizeRemainingDebts(
      Map<DebtKey, List<DebtFragment>> debts, String currency) {
    // Sum fragments sharing a direction and omit fully settled balances.
    List<DirectedDebt> balances =
        debts.entrySet().stream()
            .map(
                entry ->
                    DirectedDebt.builder()
                        .participants(entry.getKey())
                        .remainingAmount(
                            entry.getValue().stream()
                                .map(DebtFragment::getRemainingAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .build())
            .filter(balance -> balance.remainingAmount().signum() > 0)
            .sorted(
                Comparator.comparing((DirectedDebt balance) -> balance.participants().debtorId())
                    .thenComparing(balance -> balance.participants().creditorId()))
            .toList();

    return GroupBalanceResult.builder().currency(currency).balances(balances).build();
  }

  /** The amount left after consumption and the fragments consumed along the way. */
  private record ConsumptionResult(
      BigDecimal remainingAmount, List<ConsumedFragment> consumedFragments) {}

  /** One consumed fragment's origin and amount, independent of the transaction causing it. */
  private record ConsumedFragment(UUID originExpenseId, BigDecimal appliedAmount) {}
}
