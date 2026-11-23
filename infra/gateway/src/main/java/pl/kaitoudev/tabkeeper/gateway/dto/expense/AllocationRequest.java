package pl.kaitoudev.tabkeeper.gateway.dto.expense;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** A participant and their share in a new expense. */
public record AllocationRequest(@NotNull UUID userId, @Valid @NotNull MonetaryRequest amount) {}
