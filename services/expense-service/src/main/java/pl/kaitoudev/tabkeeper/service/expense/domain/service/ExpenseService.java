package pl.kaitoudev.tabkeeper.service.expense.domain.service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.kaitoudev.tabkeeper.service.expense.client.GroupGrpcClient;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.Expense;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.ExpenseAllocation;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.ExpenseGroup;
import pl.kaitoudev.tabkeeper.service.expense.domain.exception.InvalidExpenseException;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.GroupDetails;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.UserExpenseHistory;
import pl.kaitoudev.tabkeeper.service.expense.domain.repository.ExpenseGroupRepository;
import pl.kaitoudev.tabkeeper.service.expense.domain.repository.ExpenseRepository;

/** Creates expenses and validates their financial and group invariants. */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExpenseService {

  private static final int MAX_AMOUNT_PRECISION = 17;
  private static final int AMOUNT_SCALE = 2;

  private final ExpenseRepository expenseRepository;
  private final ExpenseGroupRepository expenseGroupRepository;
  private final GroupGrpcClient groupGrpcClient;

  /**
   * Validates and persists an expense, its participant shares, and its group-local sequence.
   *
   * <p>Group details are fetched before acquiring the local coordination lock. The group row lock
   * serializes subsequent expense and repayment writes for the same group.
   *
   * @param groupId identifier of the group to which the expense belongs
   * @param payingUserId identifier of the participant who paid
   * @param amount total expense amount, including the payer's own share
   * @param currency ISO-style three-letter currency code expected by the group
   * @param allocations participant shares to associate with the new expense
   * @return the persisted expense with its participant allocations
   */
  @Transactional
  public Expense createExpense(
      UUID groupId,
      UUID payingUserId,
      BigDecimal amount,
      String currency,
      List<ExpenseAllocation> allocations) {
    GroupDetails group = groupGrpcClient.getGroup(groupId);
    validateExpense(group, payingUserId, amount, currency, allocations);

    expenseGroupRepository.ensureExists(groupId);
    ExpenseGroup expenseGroup =
        expenseGroupRepository
            .findByGroupIdForUpdate(groupId)
            .orElseThrow(
                () -> new IllegalStateException("Expense group coordination row is absent."));
    long sequence = expenseGroup.allocateNextSequence();

    Expense expense =
        Expense.builder()
            .group(expenseGroup)
            .payingUserId(payingUserId)
            .amount(amount)
            .currency(currency)
            .sequence(sequence)
            .build();
    allocations.forEach(expense::addAllocation);

    Expense savedExpense = expenseRepository.saveAndFlush(expense);
    log.info(
        "ExpenseCreated: ID={}, GroupID={}, PayingUserID={}, Sequence={}",
        savedExpense.getId(),
        groupId,
        payingUserId,
        sequence);

    return savedExpense;
  }

  /** Validates the expense against the current group snapshot and monetary constraints. */
  private void validateExpense(
      GroupDetails group,
      UUID payingUserId,
      BigDecimal amount,
      String currency,
      List<ExpenseAllocation> allocations) {
    if (!group.currency().equals(currency)) {
      throw InvalidExpenseException.ofMessage(
          "Expense currency must match the group's currency %s.".formatted(group.currency()));
    }
    validateAmount(amount, "Expense amount", false);

    if (allocations.isEmpty()) {
      throw InvalidExpenseException.ofMessage("An expense requires at least one allocation.");
    }
    validateUserBelongsToGroup(payingUserId, group);

    Set<UUID> participantIds = new HashSet<>();
    BigDecimal allocatedTotal = BigDecimal.ZERO;
    for (ExpenseAllocation allocation : allocations) {
      UUID participantId = allocation.getUserId();
      if (!group.memberIds().contains(participantId)) {
        throw InvalidExpenseException.ofMessage(
            "User %s is not a member of the group.".formatted(participantId));
      }
      if (!participantIds.add(participantId)) {
        throw InvalidExpenseException.ofMessage(
            "User %s has more than one allocation.".formatted(participantId));
      }

      validateAmount(allocation.getAmount(), "Allocation amount", true);
      allocatedTotal = allocatedTotal.add(allocation.getAmount());
    }

    if (allocatedTotal.compareTo(amount) != 0) {
      throw InvalidExpenseException.ofMessage(
          "Allocation amounts must total the expense amount of %s."
              .formatted(amount.toPlainString()));
    }
  }

  private static void validateUserBelongsToGroup(UUID userId, GroupDetails group) {
    if (!group.isMember(userId)) {
      throw InvalidExpenseException.ofMessage("The paying user must be a member of the group.");
    }
  }

  /** Checks a positive or non-negative exact monetary value against the schema's limits. */
  private static void validateAmount(BigDecimal amount, String fieldName, boolean allowZero) {
    if (amount == null) {
      throw InvalidExpenseException.ofMessage("%s is required.".formatted(fieldName));
    }
    if (amount.scale() != AMOUNT_SCALE || amount.precision() > MAX_AMOUNT_PRECISION) {
      throw InvalidExpenseException.ofMessage(
          "%s must have exactly two decimal places and at most 17 digits.".formatted(fieldName));
    }
    if (allowZero ? amount.signum() < 0 : amount.signum() <= 0) {
      throw InvalidExpenseException.ofMessage(
          "%s must be %s.".formatted(fieldName, allowZero ? "non-negative" : "positive"));
    }
  }

  public Optional<Expense> getExpense(UUID expenseId) {
    return expenseRepository.findByIdWithAllocations(expenseId);
  }

  public List<Expense> getAllExpensesInGroup(UUID groupId) {
    // No need for a call to the gRPC service at first. We may have it in our
    Optional<ExpenseGroup> optExpenseGroup =
        expenseGroupRepository.findByGroupIdWithExpensesAndAllocations(groupId);

    List<Expense> expenses = new ArrayList<>();

    optExpenseGroup.ifPresentOrElse(
        expenseGroup -> expenses.addAll(expenseGroup.getGroupExpenses()),
        () -> {
          // Will throw if it doesn't exist.
          groupGrpcClient.getGroup(groupId);
        });

    expenses.sort(Comparator.comparingLong(Expense::getSequence));

    log.info("Retrieved all expenses in group {}. {} expenses found.", groupId, expenses.size());

    return Collections.unmodifiableList(expenses);
  }

  /** Retrieves a group member's paid and allocated expense histories with one group lookup. */
  public UserExpenseHistory getAllExpensesInGroupOfUser(UUID groupId, UUID userId) {
    GroupDetails group = groupGrpcClient.getGroup(groupId);
    validateUserBelongsToGroup(userId, group);

    List<Expense> paidExpenses =
        expenseRepository.findAllPaidByUserWithAllocations(groupId, userId);
    List<Expense> allocatedExpenses =
        expenseRepository.findAllAllocatedToUserWithAllocations(groupId, userId);

    log.info(
        "Retrieved expenses in group {} for user {}: {} paid, {} allocated.",
        groupId,
        userId,
        paidExpenses.size(),
        allocatedExpenses.size());

    return new UserExpenseHistory(paidExpenses, allocatedExpenses);
  }
}
