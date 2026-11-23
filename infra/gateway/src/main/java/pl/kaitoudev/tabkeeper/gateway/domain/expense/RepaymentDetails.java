package pl.kaitoudev.tabkeeper.gateway.domain.expense;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/** A repayment and the expense obligations it settled. */
@Builder
public record RepaymentDetails(
    UUID repaymentId,
    UUID groupId,
    UUID reportingUserId,
    UUID repayingUserId,
    UUID recipientUserId,
    Monetary moneyAmount,
    Instant createdAt,
    List<RepaymentAllocationDetails> allocations) {}
