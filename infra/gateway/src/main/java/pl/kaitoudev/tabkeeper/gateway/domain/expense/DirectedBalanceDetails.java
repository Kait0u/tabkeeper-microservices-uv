package pl.kaitoudev.tabkeeper.gateway.domain.expense;

import java.util.UUID;
import lombok.Builder;

/** An outstanding directed debt between two group members. */
@Builder
public record DirectedBalanceDetails(
    UUID debtorUserId, UUID creditorUserId, Monetary remainingAmount) {}
