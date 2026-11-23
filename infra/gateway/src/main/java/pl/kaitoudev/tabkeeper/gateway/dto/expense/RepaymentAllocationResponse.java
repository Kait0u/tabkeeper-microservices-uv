package pl.kaitoudev.tabkeeper.gateway.dto.expense;

import java.util.UUID;
import lombok.Builder;

/** A repayment portion applied to an expense-derived obligation. */
@Builder
public record RepaymentAllocationResponse(UUID originExpenseId, MonetaryResponse appliedAmount) {}
