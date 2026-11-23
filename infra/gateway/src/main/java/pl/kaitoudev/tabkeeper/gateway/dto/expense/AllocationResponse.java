package pl.kaitoudev.tabkeeper.gateway.dto.expense;

import java.util.UUID;
import lombok.Builder;

/** A participant allocation returned with an expense. */
@Builder
public record AllocationResponse(UUID userId, MonetaryResponse amount) {}
