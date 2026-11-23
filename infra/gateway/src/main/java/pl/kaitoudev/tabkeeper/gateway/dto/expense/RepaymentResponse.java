package pl.kaitoudev.tabkeeper.gateway.dto.expense;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/** Repayment details returned by creation, retrieval, and history endpoints. */
@Builder
public record RepaymentResponse(
    UUID repaymentId,
    UUID groupId,
    UUID reportingUserId,
    UUID repayingUserId,
    UUID recipientUserId,
    MonetaryResponse moneyAmount,
    Instant createdAt,
    List<RepaymentAllocationResponse> allocations) {}
