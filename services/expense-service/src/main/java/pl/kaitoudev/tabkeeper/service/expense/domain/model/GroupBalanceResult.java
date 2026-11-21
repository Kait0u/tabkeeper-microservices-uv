package pl.kaitoudev.tabkeeper.service.expense.domain.model;

import java.util.List;
import lombok.Builder;

@Builder
public record GroupBalanceResult(String currency, List<DirectedDebt> balances) {}
