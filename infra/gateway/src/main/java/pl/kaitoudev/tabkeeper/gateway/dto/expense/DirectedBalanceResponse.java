package pl.kaitoudev.tabkeeper.gateway.dto.expense;

import java.util.UUID;
import lombok.Builder;

/** A directed outstanding balance returned by the REST API. */
@Builder
public record DirectedBalanceResponse(
    UUID debtorUserId, UUID creditorUserId, MonetaryResponse remainingAmount) {}
