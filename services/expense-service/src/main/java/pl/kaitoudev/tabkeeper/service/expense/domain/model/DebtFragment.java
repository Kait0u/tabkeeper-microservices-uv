package pl.kaitoudev.tabkeeper.service.expense.domain.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public final class DebtFragment {
  private final UUID originExpenseId;
  private final long originSequence;
  private BigDecimal remainingAmount;

  public BigDecimal consume(BigDecimal requestedAmount) {
    BigDecimal appliedAmount = requestedAmount.min(remainingAmount);
    remainingAmount = remainingAmount.subtract(appliedAmount);
    return appliedAmount;
  }

  public boolean isSettled() {
    return remainingAmount.signum() == 0;
  }
}
