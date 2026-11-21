package pl.kaitoudev.tabkeeper.service.expense.domain.model;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record DirectedDebt(DebtKey participants, BigDecimal remainingAmount) {}
