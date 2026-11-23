package pl.kaitoudev.tabkeeper.gateway.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kaitoudev.tabkeeper.gateway.client.ExpenseGrpcClient;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.ExpenseDetails;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.CreateExpenseRequest;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.ExpenseResponse;
import pl.kaitoudev.tabkeeper.gateway.mapper.FinancialRestMapper;

/** Exposes expense creation and direct retrieval through the gateway. */
@Tag(name = "Expenses")
@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {
  private final ExpenseGrpcClient expenseGrpcClient;

  /** Creates an expense and returns its stored representation. */
  @PostMapping
  public ResponseEntity<ExpenseResponse> createExpense(
      @Valid @RequestBody CreateExpenseRequest request) {
    ExpenseDetails expense =
        expenseGrpcClient.createExpense(
            request.groupId().toString(),
            request.payingUserId().toString(),
            FinancialRestMapper.toDomain(request.moneyAmount()),
            FinancialRestMapper.toDomainAllocations(request.allocations()));

    URI location = URI.create("/api/v1/expenses/" + expense.expenseId());
    return ResponseEntity.created(location).body(FinancialRestMapper.toResponse(expense));
  }

  /** Retrieves an expense by identifier. */
  @GetMapping("/{expense_id}")
  public ResponseEntity<ExpenseResponse> getExpense(@PathVariable("expense_id") UUID expenseId) {
    ExpenseDetails expense = expenseGrpcClient.getExpense(expenseId.toString());

    return ResponseEntity.ok(FinancialRestMapper.toResponse(expense));
  }
}
