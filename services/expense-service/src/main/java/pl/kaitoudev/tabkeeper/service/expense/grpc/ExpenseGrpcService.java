package pl.kaitoudev.tabkeeper.service.expense.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.grpc.server.service.GrpcService;
import pl.kaitoudev.tabkeeper.proto.expense.v1.CreateExpenseRequest;
import pl.kaitoudev.tabkeeper.proto.expense.v1.CreateRepaymentRequest;
import pl.kaitoudev.tabkeeper.proto.expense.v1.DirectedBalance;
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
import pl.kaitoudev.tabkeeper.service.expense.client.exception.GroupServiceRequestException;
import pl.kaitoudev.tabkeeper.service.expense.client.exception.GroupServiceUnavailableException;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.Expense;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.ExpenseAllocation;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.Repayment;
import pl.kaitoudev.tabkeeper.service.expense.domain.exception.InvalidExpenseException;
import pl.kaitoudev.tabkeeper.service.expense.domain.exception.InvalidRequestException;
import pl.kaitoudev.tabkeeper.service.expense.domain.exception.NonexistentGroupException;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.DirectedDebt;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.GroupBalanceResult;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.RepaymentDetails;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.UserExpenseHistory;
import pl.kaitoudev.tabkeeper.service.expense.domain.service.ExpenseService;
import pl.kaitoudev.tabkeeper.shared.grpc.GrpcMappingUtils;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ExpenseGrpcService extends ExpenseServiceGrpc.ExpenseServiceImplBase {

  private final ExpenseService expenseService;

  /** Creates an expense and returns its persisted details, including participant shares. */
  @Override
  public void createExpense(
      CreateExpenseRequest request, StreamObserver<ExpenseInfo> responseObserver) {
    MonetaryAmount moneyAmount = request.getMoneyAmount();
    List<ExpenseAllocation> allocations =
        request.getAllocationsList().stream()
            .map(allocation -> toExpenseAllocation(allocation, moneyAmount.getCurrency()))
            .toList();

    Expense expense;
    try {
      expense =
          expenseService.createExpense(
              toUuid(request.getGroupId(), "Group ID"),
              toUuid(request.getPayingUserId(), "Paying user ID"),
              toBigDecimal(moneyAmount, "Expense amount"),
              moneyAmount.getCurrency(),
              allocations);
    } catch (NonexistentGroupException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceUnavailableException ex) {
      responseObserver.onError(
          Status.UNAVAILABLE.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceRequestException ex) {
      responseObserver.onError(
          Status.INTERNAL
              .withCause(ex)
              .withDescription("Group Service could not provide the requested group.")
              .asRuntimeException());
      return;
    }

    ExpenseInfo response = toExpenseInfo(expense);

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  private static @NonNull ExpenseInfo toExpenseInfo(Expense expense) {
    return ExpenseInfo.newBuilder()
        .setExpenseId(expense.getId().toString())
        .setGroupId(expense.getGroup().getGroupId().toString())
        .setPayingUserId(expense.getPayingUserId().toString())
        .setMoneyAmount(
            toMonetaryAmount(expense.getAmount().toPlainString(), expense.getCurrency()))
        .addAllAllocations(
            expense.getAllocations().stream()
                .sorted(Comparator.comparing(ExpenseAllocation::getUserId))
                .map(
                    allocation ->
                        pl.kaitoudev.tabkeeper.proto.expense.v1.ExpenseAllocation.newBuilder()
                            .setUserId(allocation.getUserId().toString())
                            .setAmount(
                                toMonetaryAmount(
                                    allocation.getAmount().toPlainString(), expense.getCurrency()))
                            .build())
                .toList())
        .setCreatedTs(GrpcMappingUtils.toProtobufTimestamp(expense.getCreatedTs()))
        .build();
  }

  /** Maps a participant share and rejects a currency that differs from the expense currency. */
  private static ExpenseAllocation toExpenseAllocation(
      pl.kaitoudev.tabkeeper.proto.expense.v1.ExpenseAllocation allocation, String currency) {
    if (!currency.equals(allocation.getAmount().getCurrency())) {
      throw InvalidExpenseException.ofMessage("Every allocation must use the expense's currency.");
    }

    return ExpenseAllocation.builder()
        .userId(toUuid(allocation.getUserId(), "Allocation user ID"))
        .amount(toBigDecimal(allocation.getAmount(), "Allocation amount"))
        .build();
  }

  /** Parses a UUID while preserving malformed input as a domain validation failure. */
  private static UUID toUuid(String value, String fieldName) {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException ex) {
      throw InvalidRequestException.ofMessage("%s must be a valid UUID.".formatted(fieldName), ex);
    }
  }

  /** Parses an exact decimal monetary value without leaking a Java parsing exception. */
  private static BigDecimal toBigDecimal(MonetaryAmount amount, String fieldName) {
    try {
      return new BigDecimal(amount.getValue());
    } catch (NumberFormatException ex) {
      throw InvalidRequestException.ofMessage(
          "%s must be an exact decimal value.".formatted(fieldName), ex);
    }
  }

  /** Creates a protobuf monetary amount with the supplied exact decimal representation. */
  private static MonetaryAmount toMonetaryAmount(String value, String currency) {
    return MonetaryAmount.newBuilder().setValue(value).setCurrency(currency).build();
  }

  @Override
  public void getExpense(GetExpenseRequest request, StreamObserver<ExpenseInfo> responseObserver) {
    UUID expenseId = toUuid(request.getExpenseId(), "Expense ID");

    Optional<Expense> optExpense = expenseService.getExpense(expenseId);

    if (optExpense.isEmpty()) {
      responseObserver.onError(
          Status.NOT_FOUND
              .withDescription("Expense ID=%s not found.".formatted(expenseId.toString()))
              .asRuntimeException());
      return;
    }

    Expense expense = optExpense.get();
    ExpenseInfo response = toExpenseInfo(expense);

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getAllExpensesInGroup(
      GetAllExpensesInGroupRequest request, StreamObserver<ExpensesInGroup> responseObserver) {
    UUID groupId = toUuid(request.getGroupId(), "Group ID");

    List<Expense> expensesInGroup;
    try {
      expensesInGroup = expenseService.getAllExpensesInGroup(groupId);
    } catch (NonexistentGroupException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceUnavailableException ex) {
      responseObserver.onError(
          Status.UNAVAILABLE.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceRequestException ex) {
      responseObserver.onError(
          Status.INTERNAL
              .withCause(ex)
              .withDescription("Group Service could not provide the requested group.")
              .asRuntimeException());
      return;
    }

    ExpensesInGroup response =
        ExpensesInGroup.newBuilder()
            .setGroupId(groupId.toString())
            .addAllExpenses(
                expensesInGroup.stream().map(ExpenseGrpcService::toExpenseInfo).toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getAllExpensesInGroupOfUser(
      GetAllExpensesInGroupOfUserRequest request,
      StreamObserver<ExpensesInGroupOfUser> responseObserver) {
    UUID groupId = toUuid(request.getGroupId(), "Group ID");
    UUID userId = toUuid(request.getUserId(), "User ID");

    UserExpenseHistory expenseHistory;
    try {
      expenseHistory = expenseService.getAllExpensesInGroupOfUser(groupId, userId);
    } catch (NonexistentGroupException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceUnavailableException ex) {
      responseObserver.onError(
          Status.UNAVAILABLE.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceRequestException ex) {
      responseObserver.onError(
          Status.INTERNAL
              .withCause(ex)
              .withDescription("Group Service could not provide the requested group.")
              .asRuntimeException());
      return;
    }

    ExpensesInGroupOfUser response =
        ExpensesInGroupOfUser.newBuilder()
            .setGroupId(groupId.toString())
            .setUserId(userId.toString())
            .addAllExpensesPaid(
                expenseHistory.paidExpenses().stream()
                    .map(ExpenseGrpcService::toExpenseInfo)
                    .toList())
            .addAllExpensesAllocated(
                expenseHistory.allocatedExpenses().stream()
                    .map(ExpenseGrpcService::toExpenseInfo)
                    .toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getGroupBalance(
      GetGroupBalanceRequest request, StreamObserver<GroupBalance> responseObserver) {
    UUID groupId = toUuid(request.getGroupId(), "Group ID");

    GroupBalanceResult balance;
    try {
      balance = expenseService.getGroupBalance(groupId);
    } catch (NonexistentGroupException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceUnavailableException ex) {
      responseObserver.onError(
          Status.UNAVAILABLE.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceRequestException ex) {
      responseObserver.onError(
          Status.INTERNAL
              .withCause(ex)
              .withDescription("Group Service could not provide the requested group.")
              .asRuntimeException());
      return;
    }

    GroupBalance response =
        GroupBalance.newBuilder()
            .setGroupId(groupId.toString())
            .addAllBalances(
                balance.balances().stream()
                    .map(debt -> toDirectedBalance(debt, balance.currency()))
                    .toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  /** Maps a directed domain debt to its protobuf representation. */
  private static DirectedBalance toDirectedBalance(DirectedDebt debt, String currency) {
    return DirectedBalance.newBuilder()
        .setDebtorUserId(debt.participants().debtorId().toString())
        .setCreditorUserId(debt.participants().creditorId().toString())
        .setRemainingAmount(toMonetaryAmount(debt.remainingAmount().toPlainString(), currency))
        .build();
  }

  @Override
  public void createRepayment(
      CreateRepaymentRequest request, StreamObserver<RepaymentInfo> responseObserver) {
    UUID groupId = toUuid(request.getGroupId(), "Group ID");
    UUID repayingUserId = toUuid(request.getRepayingUserId(), "Repaying user ID");
    UUID recipientUserId = toUuid(request.getRecipientUserId(), "Recipient user ID");
    UUID reportingUserId = toUuid(request.getReportingUserId(), "Reporting user ID");
    MonetaryAmount moneyAmount = request.getMoneyAmount();
    BigDecimal amount = toBigDecimal(moneyAmount, "Repayment amount");

    RepaymentDetails repaymentDetails;
    try {
      repaymentDetails =
          expenseService.createRepayment(
              groupId,
              repayingUserId,
              recipientUserId,
              amount,
              moneyAmount.getCurrency(),
              reportingUserId);
    } catch (NonexistentGroupException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceUnavailableException ex) {
      responseObserver.onError(
          Status.UNAVAILABLE.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceRequestException ex) {
      responseObserver.onError(
          Status.INTERNAL
              .withCause(ex)
              .withDescription("Group Service could not provide the requested group.")
              .asRuntimeException());
      return;
    }

    responseObserver.onNext(toRepaymentInfo(repaymentDetails));
    responseObserver.onCompleted();
  }

  /** Maps a persisted repayment and its FIFO allocations to the protobuf response. */
  private static RepaymentInfo toRepaymentInfo(RepaymentDetails repaymentDetails) {
    Repayment repayment = repaymentDetails.repayment();

    return RepaymentInfo.newBuilder()
        .setRepaymentId(repayment.getId().toString())
        .setGroupId(repayment.getGroup().getGroupId().toString())
        .setReportingUserId(repayment.getReportingUserId().toString())
        .setRepayingUserId(repayment.getRepayingUserId().toString())
        .setRecipientUserId(repayment.getRecipientUserId().toString())
        .setMoneyAmount(
            toMonetaryAmount(repayment.getAmount().toPlainString(), repayment.getCurrency()))
        .setCreatedTs(GrpcMappingUtils.toProtobufTimestamp(repayment.getCreatedTs()))
        .addAllAllocations(
            repaymentDetails.allocations().stream()
                .map(
                    allocation ->
                        RepaymentAllocationInfo.newBuilder()
                            .setOriginExpenseId(allocation.originExpenseId().toString())
                            .setAppliedAmount(
                                toMonetaryAmount(
                                    allocation.appliedAmount().toPlainString(),
                                    repayment.getCurrency()))
                            .build())
                .toList())
        .build();
  }

  @Override
  public void getRepayment(
      GetRepaymentRequest request, StreamObserver<RepaymentInfo> responseObserver) {
    UUID groupId = toUuid(request.getGroupId(), "Group ID");
    UUID repaymentId = toUuid(request.getRepaymentId(), "Repayment ID");

    Optional<RepaymentDetails> repaymentDetails;
    try {
      repaymentDetails = expenseService.getRepayment(groupId, repaymentId);
    } catch (NonexistentGroupException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceUnavailableException ex) {
      responseObserver.onError(
          Status.UNAVAILABLE.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceRequestException ex) {
      responseObserver.onError(
          Status.INTERNAL
              .withCause(ex)
              .withDescription("Group Service could not provide the requested group.")
              .asRuntimeException());
      return;
    }

    if (repaymentDetails.isEmpty()) {
      responseObserver.onError(
          Status.NOT_FOUND
              .withDescription(
                  "Repayment ID=%s not found in group ID=%s.".formatted(repaymentId, groupId))
              .asRuntimeException());
      return;
    }

    responseObserver.onNext(toRepaymentInfo(repaymentDetails.get()));
    responseObserver.onCompleted();
  }

  @Override
  public void getAllRepaymentsInGroup(
      GetAllRepaymentsInGroupRequest request, StreamObserver<RepaymentsInGroup> responseObserver) {
    UUID groupId = toUuid(request.getGroupId(), "Group ID");

    List<RepaymentDetails> repayments;
    try {
      repayments = expenseService.getAllRepaymentsInGroup(groupId);
    } catch (NonexistentGroupException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceUnavailableException ex) {
      responseObserver.onError(
          Status.UNAVAILABLE.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceRequestException ex) {
      responseObserver.onError(
          Status.INTERNAL
              .withCause(ex)
              .withDescription("Group Service could not provide the requested group.")
              .asRuntimeException());
      return;
    }

    RepaymentsInGroup response =
        RepaymentsInGroup.newBuilder()
            .setGroupId(groupId.toString())
            .addAllRepayments(repayments.stream().map(ExpenseGrpcService::toRepaymentInfo).toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getAllRepaymentsInGroupOfUser(
      GetAllRepaymentsInGroupOfUserRequest request,
      StreamObserver<RepaymentsInGroupOfUser> responseObserver) {
    UUID groupId = toUuid(request.getGroupId(), "Group ID");
    UUID userId = toUuid(request.getUserId(), "User ID");

    List<RepaymentDetails> repayments;
    try {
      repayments = expenseService.getAllRepaymentsInGroupOfUser(groupId, userId);
    } catch (NonexistentGroupException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceUnavailableException ex) {
      responseObserver.onError(
          Status.UNAVAILABLE.withCause(ex).withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (GroupServiceRequestException ex) {
      responseObserver.onError(
          Status.INTERNAL
              .withCause(ex)
              .withDescription("Group Service could not provide the requested group.")
              .asRuntimeException());
      return;
    }

    RepaymentsInGroupOfUser response =
        RepaymentsInGroupOfUser.newBuilder()
            .setGroupId(groupId.toString())
            .setUserId(userId.toString())
            .addAllRepayments(repayments.stream().map(ExpenseGrpcService::toRepaymentInfo).toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
