package pl.kaitoudev.tabkeeper.service.expense.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
import pl.kaitoudev.tabkeeper.proto.expense.v1.CreateExpenseRequest;
import pl.kaitoudev.tabkeeper.proto.expense.v1.CreateRepaymentRequest;
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
import pl.kaitoudev.tabkeeper.proto.expense.v1.RepaymentInfo;
import pl.kaitoudev.tabkeeper.proto.expense.v1.RepaymentsInGroup;
import pl.kaitoudev.tabkeeper.proto.expense.v1.RepaymentsInGroupOfUser;
import pl.kaitoudev.tabkeeper.service.expense.client.exception.GroupServiceRequestException;
import pl.kaitoudev.tabkeeper.service.expense.client.exception.GroupServiceUnavailableException;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.Expense;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.ExpenseAllocation;
import pl.kaitoudev.tabkeeper.service.expense.domain.exception.InvalidExpenseException;
import pl.kaitoudev.tabkeeper.service.expense.domain.exception.NonexistentGroupException;
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
              UUID.fromString(request.getGroupId()),
              UUID.fromString(request.getPayingUserId()),
              new BigDecimal(moneyAmount.getValue()),
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

    ExpenseInfo response =
        ExpenseInfo.newBuilder()
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
                                        allocation.getAmount().toPlainString(),
                                        expense.getCurrency()))
                                .build())
                    .toList())
            .setCreatedTs(GrpcMappingUtils.toProtobufTimestamp(expense.getCreatedTs()))
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  /** Maps a participant share and rejects a currency that differs from the expense currency. */
  private static ExpenseAllocation toExpenseAllocation(
      pl.kaitoudev.tabkeeper.proto.expense.v1.ExpenseAllocation allocation, String currency) {
    if (!currency.equals(allocation.getAmount().getCurrency())) {
      throw InvalidExpenseException.ofMessage("Every allocation must use the expense's currency.");
    }

    return ExpenseAllocation.builder()
        .userId(UUID.fromString(allocation.getUserId()))
        .amount(new BigDecimal(allocation.getAmount().getValue()))
        .build();
  }

  /** Creates a protobuf monetary amount with the supplied exact decimal representation. */
  private static MonetaryAmount toMonetaryAmount(String value, String currency) {
    return MonetaryAmount.newBuilder().setValue(value).setCurrency(currency).build();
  }

  @Override
  public void getExpense(GetExpenseRequest request, StreamObserver<ExpenseInfo> responseObserver) {
    super.getExpense(request, responseObserver);
  }

  @Override
  public void getAllExpensesInGroup(
      GetAllExpensesInGroupRequest request, StreamObserver<ExpensesInGroup> responseObserver) {
    super.getAllExpensesInGroup(request, responseObserver);
  }

  @Override
  public void getAllExpensesInGroupOfUser(
      GetAllExpensesInGroupOfUserRequest request,
      StreamObserver<ExpensesInGroupOfUser> responseObserver) {
    super.getAllExpensesInGroupOfUser(request, responseObserver);
  }

  @Override
  public void getGroupBalance(
      GetGroupBalanceRequest request, StreamObserver<GroupBalance> responseObserver) {
    super.getGroupBalance(request, responseObserver);
  }

  @Override
  public void createRepayment(
      CreateRepaymentRequest request, StreamObserver<RepaymentInfo> responseObserver) {
    super.createRepayment(request, responseObserver);
  }

  @Override
  public void getRepayment(
      GetRepaymentRequest request, StreamObserver<RepaymentInfo> responseObserver) {
    super.getRepayment(request, responseObserver);
  }

  @Override
  public void getAllRepaymentsInGroup(
      GetAllRepaymentsInGroupRequest request, StreamObserver<RepaymentsInGroup> responseObserver) {
    super.getAllRepaymentsInGroup(request, responseObserver);
  }

  @Override
  public void getAllRepaymentsInGroupOfUser(
      GetAllRepaymentsInGroupOfUserRequest request,
      StreamObserver<RepaymentsInGroupOfUser> responseObserver) {
    super.getAllRepaymentsInGroupOfUser(request, responseObserver);
  }
}
