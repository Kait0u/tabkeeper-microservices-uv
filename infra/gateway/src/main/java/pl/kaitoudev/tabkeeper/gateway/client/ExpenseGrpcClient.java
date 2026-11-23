package pl.kaitoudev.tabkeeper.gateway.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.Allocation;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.DirectedBalanceDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.ExpenseDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.GroupBalanceDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.Monetary;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.RepaymentAllocationDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.RepaymentDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.UserExpenseHistoryDetails;
import pl.kaitoudev.tabkeeper.proto.expense.v1.CreateExpenseRequest;
import pl.kaitoudev.tabkeeper.proto.expense.v1.CreateRepaymentRequest;
import pl.kaitoudev.tabkeeper.proto.expense.v1.DirectedBalance;
import pl.kaitoudev.tabkeeper.proto.expense.v1.ExpenseAllocation;
import pl.kaitoudev.tabkeeper.proto.expense.v1.ExpenseInfo;
import pl.kaitoudev.tabkeeper.proto.expense.v1.ExpenseServiceGrpc;
import pl.kaitoudev.tabkeeper.proto.expense.v1.ExpensesInGroup;
import pl.kaitoudev.tabkeeper.proto.expense.v1.ExpensesInGroupOfUser;
import pl.kaitoudev.tabkeeper.proto.expense.v1.GetAllExpensesInGroupOfUserRequest;
import pl.kaitoudev.tabkeeper.proto.expense.v1.GetAllExpensesInGroupRequest;
import pl.kaitoudev.tabkeeper.proto.expense.v1.GetAllRepaymentsInGroupOfUserRequest;
import pl.kaitoudev.tabkeeper.proto.expense.v1.GetAllRepaymentsInGroupRequest;
import pl.kaitoudev.tabkeeper.proto.expense.v1.GetExpenseRequest;
import pl.kaitoudev.tabkeeper.proto.expense.v1.GetGroupBalanceRequest;
import pl.kaitoudev.tabkeeper.proto.expense.v1.GetRepaymentRequest;
import pl.kaitoudev.tabkeeper.proto.expense.v1.GroupBalance;
import pl.kaitoudev.tabkeeper.proto.expense.v1.MonetaryAmount;
import pl.kaitoudev.tabkeeper.proto.expense.v1.RepaymentAllocationInfo;
import pl.kaitoudev.tabkeeper.proto.expense.v1.RepaymentInfo;
import pl.kaitoudev.tabkeeper.proto.expense.v1.RepaymentsInGroup;
import pl.kaitoudev.tabkeeper.proto.expense.v1.RepaymentsInGroupOfUser;
import pl.kaitoudev.tabkeeper.shared.grpc.GrpcMappingUtils;
import pl.kaitoudev.tabkeeper.shared.grpc.availability.GrpcStubHolder;

/** Calls Expense Service and maps its protobuf models to gateway domain models. */
@Component
@RequiredArgsConstructor
public class ExpenseGrpcClient {
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(3);
  private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(5);

  private final GrpcStubHolder<ExpenseServiceGrpc.ExpenseServiceBlockingStub> holder;

  /** Creates an expense with its participant allocations. */
  public ExpenseDetails createExpense(
      String groupId, String payingUserId, Monetary moneyAmount, List<Allocation> allocations) {
    CreateExpenseRequest request =
        CreateExpenseRequest.newBuilder()
            .setGroupId(groupId)
            .setPayingUserId(payingUserId)
            .setMoneyAmount(toMonetaryAmount(moneyAmount))
            .addAllAllocations(
                allocations.stream().map(ExpenseGrpcClient::toProtoAllocation).toList())
            .build();

    ExpenseInfo response = holder.getStub().withDeadlineAfter(WRITE_TIMEOUT).createExpense(request);

    return toExpenseDetails(response);
  }

  /** Retrieves an expense by identifier. */
  public ExpenseDetails getExpense(String expenseId) {
    GetExpenseRequest request = GetExpenseRequest.newBuilder().setExpenseId(expenseId).build();
    ExpenseInfo response = holder.getStub().withDeadlineAfter(READ_TIMEOUT).getExpense(request);

    return toExpenseDetails(response);
  }

  /** Lists all expenses in a group in service-defined order. */
  public List<ExpenseDetails> getAllExpensesInGroup(String groupId) {
    GetAllExpensesInGroupRequest request =
        GetAllExpensesInGroupRequest.newBuilder().setGroupId(groupId).build();
    ExpensesInGroup response =
        holder.getStub().withDeadlineAfter(READ_TIMEOUT).getAllExpensesInGroup(request);

    return response.getExpensesList().stream().map(ExpenseGrpcClient::toExpenseDetails).toList();
  }

  /** Retrieves a member's paid and allocated expense histories. */
  public UserExpenseHistoryDetails getAllExpensesInGroupOfUser(String groupId, String userId) {
    GetAllExpensesInGroupOfUserRequest request =
        GetAllExpensesInGroupOfUserRequest.newBuilder()
            .setGroupId(groupId)
            .setUserId(userId)
            .build();
    ExpensesInGroupOfUser response =
        holder.getStub().withDeadlineAfter(READ_TIMEOUT).getAllExpensesInGroupOfUser(request);

    return UserExpenseHistoryDetails.builder()
        .groupId(UUID.fromString(response.getGroupId()))
        .userId(UUID.fromString(response.getUserId()))
        .expensesPaid(
            response.getExpensesPaidList().stream()
                .map(ExpenseGrpcClient::toExpenseDetails)
                .toList())
        .expensesAllocated(
            response.getExpensesAllocatedList().stream()
                .map(ExpenseGrpcClient::toExpenseDetails)
                .toList())
        .build();
  }

  /** Retrieves current directed balances for a group. */
  public GroupBalanceDetails getGroupBalance(String groupId) {
    GetGroupBalanceRequest request =
        GetGroupBalanceRequest.newBuilder().setGroupId(groupId).build();
    GroupBalance response =
        holder.getStub().withDeadlineAfter(READ_TIMEOUT).getGroupBalance(request);

    return GroupBalanceDetails.builder()
        .groupId(UUID.fromString(response.getGroupId()))
        .balances(
            response.getBalancesList().stream().map(ExpenseGrpcClient::toDirectedBalance).toList())
        .build();
  }

  /** Creates a repayment and returns its calculated FIFO allocations. */
  public RepaymentDetails createRepayment(
      String groupId,
      String reportingUserId,
      String repayingUserId,
      String recipientUserId,
      Monetary moneyAmount) {
    CreateRepaymentRequest request =
        CreateRepaymentRequest.newBuilder()
            .setGroupId(groupId)
            .setReportingUserId(reportingUserId)
            .setRepayingUserId(repayingUserId)
            .setRecipientUserId(recipientUserId)
            .setMoneyAmount(toMonetaryAmount(moneyAmount))
            .build();

    RepaymentInfo response =
        holder.getStub().withDeadlineAfter(WRITE_TIMEOUT).createRepayment(request);

    return toRepaymentDetails(response);
  }

  /** Retrieves a repayment within a specific group. */
  public RepaymentDetails getRepayment(String groupId, String repaymentId) {
    GetRepaymentRequest request =
        GetRepaymentRequest.newBuilder().setGroupId(groupId).setRepaymentId(repaymentId).build();
    RepaymentInfo response = holder.getStub().withDeadlineAfter(READ_TIMEOUT).getRepayment(request);

    return toRepaymentDetails(response);
  }

  /** Lists all repayments in a group. */
  public List<RepaymentDetails> getAllRepaymentsInGroup(String groupId) {
    GetAllRepaymentsInGroupRequest request =
        GetAllRepaymentsInGroupRequest.newBuilder().setGroupId(groupId).build();
    RepaymentsInGroup response =
        holder.getStub().withDeadlineAfter(READ_TIMEOUT).getAllRepaymentsInGroup(request);

    return response.getRepaymentsList().stream()
        .map(ExpenseGrpcClient::toRepaymentDetails)
        .toList();
  }

  /** Lists repayments where a member is the repayer or recipient. */
  public List<RepaymentDetails> getAllRepaymentsInGroupOfUser(String groupId, String userId) {
    GetAllRepaymentsInGroupOfUserRequest request =
        GetAllRepaymentsInGroupOfUserRequest.newBuilder()
            .setGroupId(groupId)
            .setUserId(userId)
            .build();
    RepaymentsInGroupOfUser response =
        holder.getStub().withDeadlineAfter(READ_TIMEOUT).getAllRepaymentsInGroupOfUser(request);

    return response.getRepaymentsList().stream()
        .map(ExpenseGrpcClient::toRepaymentDetails)
        .toList();
  }

  /** Maps a protobuf expense to its gateway representation. */
  private static ExpenseDetails toExpenseDetails(ExpenseInfo expense) {
    return ExpenseDetails.builder()
        .expenseId(UUID.fromString(expense.getExpenseId()))
        .groupId(UUID.fromString(expense.getGroupId()))
        .payingUserId(UUID.fromString(expense.getPayingUserId()))
        .moneyAmount(toMonetary(expense.getMoneyAmount()))
        .allocations(
            expense.getAllocationsList().stream().map(ExpenseGrpcClient::toAllocation).toList())
        .createdAt(GrpcMappingUtils.fromProtobufTimestamp(expense.getCreatedTs()))
        .build();
  }

  /** Maps a protobuf repayment and its allocations to gateway models. */
  private static RepaymentDetails toRepaymentDetails(RepaymentInfo repayment) {
    return RepaymentDetails.builder()
        .repaymentId(UUID.fromString(repayment.getRepaymentId()))
        .groupId(UUID.fromString(repayment.getGroupId()))
        .reportingUserId(UUID.fromString(repayment.getReportingUserId()))
        .repayingUserId(UUID.fromString(repayment.getRepayingUserId()))
        .recipientUserId(UUID.fromString(repayment.getRecipientUserId()))
        .moneyAmount(toMonetary(repayment.getMoneyAmount()))
        .createdAt(GrpcMappingUtils.fromProtobufTimestamp(repayment.getCreatedTs()))
        .allocations(
            repayment.getAllocationsList().stream()
                .map(ExpenseGrpcClient::toRepaymentAllocation)
                .toList())
        .build();
  }

  /** Maps a participant allocation returned by Expense Service. */
  private static Allocation toAllocation(ExpenseAllocation allocation) {
    return new Allocation(
        UUID.fromString(allocation.getUserId()), toMonetary(allocation.getAmount()));
  }

  /** Maps a repayment allocation returned by Expense Service. */
  private static RepaymentAllocationDetails toRepaymentAllocation(
      RepaymentAllocationInfo allocation) {
    return RepaymentAllocationDetails.builder()
        .originExpenseId(UUID.fromString(allocation.getOriginExpenseId()))
        .appliedAmount(toMonetary(allocation.getAppliedAmount()))
        .build();
  }

  /** Maps a directed balance returned by Expense Service. */
  private static DirectedBalanceDetails toDirectedBalance(DirectedBalance balance) {
    return DirectedBalanceDetails.builder()
        .debtorUserId(UUID.fromString(balance.getDebtorUserId()))
        .creditorUserId(UUID.fromString(balance.getCreditorUserId()))
        .remainingAmount(toMonetary(balance.getRemainingAmount()))
        .build();
  }

  /** Converts a gateway allocation to its protobuf request representation. */
  private static ExpenseAllocation toProtoAllocation(Allocation allocation) {
    return ExpenseAllocation.newBuilder()
        .setUserId(allocation.userId().toString())
        .setAmount(toMonetaryAmount(allocation.amount()))
        .build();
  }

  /** Converts a protobuf monetary value to an exact gateway value. */
  private static Monetary toMonetary(MonetaryAmount amount) {
    return new Monetary(new BigDecimal(amount.getValue()), amount.getCurrency());
  }

  /** Converts an exact gateway monetary value to its protobuf representation. */
  private static MonetaryAmount toMonetaryAmount(Monetary monetary) {
    return MonetaryAmount.newBuilder()
        .setValue(monetary.value().toPlainString())
        .setCurrency(monetary.currency())
        .build();
  }
}
