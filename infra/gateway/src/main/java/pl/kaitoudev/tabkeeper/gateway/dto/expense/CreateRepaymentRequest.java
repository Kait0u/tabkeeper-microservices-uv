package pl.kaitoudev.tabkeeper.gateway.dto.expense;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Repayment creation input; the reporter must be one of the participants. */
public record CreateRepaymentRequest(
    @NotNull UUID groupId,
    @NotNull UUID reportingUserId,
    @NotNull UUID repayingUserId,
    @NotNull UUID recipientUserId,
    @Valid @NotNull MonetaryRequest moneyAmount) {}
