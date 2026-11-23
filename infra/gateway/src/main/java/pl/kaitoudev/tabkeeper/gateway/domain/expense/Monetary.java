package pl.kaitoudev.tabkeeper.gateway.domain.expense;

import java.math.BigDecimal;

public record Monetary(BigDecimal value, String currency) {}
