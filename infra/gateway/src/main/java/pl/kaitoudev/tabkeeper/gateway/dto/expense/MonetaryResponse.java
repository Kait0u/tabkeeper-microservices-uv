package pl.kaitoudev.tabkeeper.gateway.dto.expense;

import java.math.BigDecimal;
import lombok.Builder;

/** Exact monetary value returned by the financial API. */
@Builder
public record MonetaryResponse(BigDecimal value, String currency) {}
