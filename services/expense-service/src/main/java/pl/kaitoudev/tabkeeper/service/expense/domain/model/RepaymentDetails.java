package pl.kaitoudev.tabkeeper.service.expense.domain.model;

import java.util.List;
import lombok.Builder;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.Repayment;

/** A repayment together with its dynamically calculated FIFO allocations. */
@Builder
public record RepaymentDetails(Repayment repayment, List<RepaymentAllocation> allocations) {}
