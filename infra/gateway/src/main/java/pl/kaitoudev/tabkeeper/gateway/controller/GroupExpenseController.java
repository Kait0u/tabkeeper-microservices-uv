package pl.kaitoudev.tabkeeper.gateway.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kaitoudev.tabkeeper.gateway.client.ExpenseGrpcClient;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.ExpenseResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.GroupBalanceResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.UserExpenseHistoryResponse;
import pl.kaitoudev.tabkeeper.gateway.mapper.FinancialRestMapper;

/** Exposes group-scoped expense histories and balances. */
@Tag(name = "Expenses")
@RestController
@RequestMapping("/api/v1/groups/{group_id}")
@RequiredArgsConstructor
public class GroupExpenseController {
  private final ExpenseGrpcClient expenseGrpcClient;

  /** Lists all expenses in a group. */
  @GetMapping("/expenses")
  public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(
      @PathVariable("group_id") UUID groupId) {
    List<ExpenseResponse> expenses =
        expenseGrpcClient.getAllExpensesInGroup(groupId.toString()).stream()
            .map(FinancialRestMapper::toResponse)
            .toList();

    return ResponseEntity.ok(expenses);
  }

  /** Retrieves a member's paid and allocated expense histories. */
  @GetMapping("/users/{user_id}/expenses")
  public ResponseEntity<UserExpenseHistoryResponse> getUserExpenses(
      @PathVariable("group_id") UUID groupId, @PathVariable("user_id") UUID userId) {
    UserExpenseHistoryResponse history =
        FinancialRestMapper.toResponse(
            expenseGrpcClient.getAllExpensesInGroupOfUser(groupId.toString(), userId.toString()));

    return ResponseEntity.ok(history);
  }

  /** Retrieves the group's current directed balances. */
  @GetMapping("/balance")
  public ResponseEntity<GroupBalanceResponse> getGroupBalance(
      @PathVariable("group_id") UUID groupId) {
    GroupBalanceResponse balance =
        FinancialRestMapper.toResponse(expenseGrpcClient.getGroupBalance(groupId.toString()));

    return ResponseEntity.ok(balance);
  }
}
