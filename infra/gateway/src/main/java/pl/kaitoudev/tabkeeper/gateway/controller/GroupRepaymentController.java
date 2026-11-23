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
import pl.kaitoudev.tabkeeper.gateway.domain.expense.RepaymentDetails;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.RepaymentResponse;
import pl.kaitoudev.tabkeeper.gateway.mapper.FinancialRestMapper;

/** Exposes group-scoped repayment retrieval and histories. */
@Tag(name = "Repayments")
@RestController
@RequestMapping("/api/v1/groups/{group_id}")
@RequiredArgsConstructor
public class GroupRepaymentController {
  private final ExpenseGrpcClient expenseGrpcClient;

  /** Retrieves a repayment within a specific group. */
  @GetMapping("/repayments/{repayment_id}")
  public ResponseEntity<RepaymentResponse> getRepayment(
      @PathVariable("group_id") UUID groupId, @PathVariable("repayment_id") UUID repaymentId) {
    RepaymentDetails repayment =
        expenseGrpcClient.getRepayment(groupId.toString(), repaymentId.toString());

    return ResponseEntity.ok(FinancialRestMapper.toResponse(repayment));
  }

  /** Lists all repayments in a group. */
  @GetMapping("/repayments")
  public ResponseEntity<List<RepaymentResponse>> getGroupRepayments(
      @PathVariable("group_id") UUID groupId) {
    List<RepaymentResponse> repayments =
        expenseGrpcClient.getAllRepaymentsInGroup(groupId.toString()).stream()
            .map(FinancialRestMapper::toResponse)
            .toList();

    return ResponseEntity.ok(repayments);
  }

  /** Lists repayments where a member is the repayer or recipient. */
  @GetMapping("/users/{user_id}/repayments")
  public ResponseEntity<List<RepaymentResponse>> getUserRepayments(
      @PathVariable("group_id") UUID groupId, @PathVariable("user_id") UUID userId) {
    List<RepaymentResponse> repayments =
        expenseGrpcClient
            .getAllRepaymentsInGroupOfUser(groupId.toString(), userId.toString())
            .stream()
            .map(FinancialRestMapper::toResponse)
            .toList();

    return ResponseEntity.ok(repayments);
  }
}
