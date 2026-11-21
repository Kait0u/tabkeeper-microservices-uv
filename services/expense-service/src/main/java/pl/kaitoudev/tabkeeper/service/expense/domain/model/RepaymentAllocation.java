package pl.kaitoudev.tabkeeper.service.expense.domain.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

/** A portion of a repayment applied to an obligation originating from an expense. */
@Builder
public record RepaymentAllocation(UUID originExpenseId, BigDecimal appliedAmount) {}
