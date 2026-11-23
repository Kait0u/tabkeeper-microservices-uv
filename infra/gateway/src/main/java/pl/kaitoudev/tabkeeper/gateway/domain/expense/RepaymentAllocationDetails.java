package pl.kaitoudev.tabkeeper.gateway.domain.expense;

import java.util.UUID;
import lombok.Builder;

/** A portion of a repayment applied to an obligation originating from an expense. */
@Builder
public record RepaymentAllocationDetails(UUID originExpenseId, Monetary appliedAmount) {}
