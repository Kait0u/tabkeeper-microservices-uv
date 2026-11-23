package pl.kaitoudev.tabkeeper.gateway.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kaitoudev.tabkeeper.gateway.client.ExpenseGrpcClient;
import pl.kaitoudev.tabkeeper.gateway.domain.expense.RepaymentDetails;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.CreateRepaymentRequest;
import pl.kaitoudev.tabkeeper.gateway.dto.expense.RepaymentResponse;
import pl.kaitoudev.tabkeeper.gateway.mapper.FinancialRestMapper;

/** Exposes repayment creation through the gateway. */
@Tag(name = "Repayments")
@RestController
@RequestMapping("/api/v1/repayments")
@RequiredArgsConstructor
public class RepaymentController {
  private final ExpenseGrpcClient expenseGrpcClient;

  /** Creates a repayment and returns its calculated FIFO allocations. */
  @PostMapping
  public ResponseEntity<RepaymentResponse> createRepayment(
      @Valid @RequestBody CreateRepaymentRequest request) {
    RepaymentDetails repayment =
        expenseGrpcClient.createRepayment(
            request.groupId().toString(),
            request.reportingUserId().toString(),
            request.repayingUserId().toString(),
            request.recipientUserId().toString(),
            FinancialRestMapper.toDomain(request.moneyAmount()));

    URI location =
        URI.create(
            "/api/v1/groups/%s/repayments/%s"
                .formatted(repayment.groupId(), repayment.repaymentId()));
    return ResponseEntity.created(location).body(FinancialRestMapper.toResponse(repayment));
  }
}
