package pl.kaitoudev.tabkeeper.service.expense.grpc;

import io.grpc.stub.StreamObserver;
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
import pl.kaitoudev.tabkeeper.proto.expense.v1.RepaymentInfo;
import pl.kaitoudev.tabkeeper.proto.expense.v1.RepaymentsInGroup;
import pl.kaitoudev.tabkeeper.proto.expense.v1.RepaymentsInGroupOfUser;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ExpenseGrpcService extends ExpenseServiceGrpc.ExpenseServiceImplBase {

  @Override
  public void createExpense(
      CreateExpenseRequest request, StreamObserver<ExpenseInfo> responseObserver) {
    super.createExpense(request, responseObserver);
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
