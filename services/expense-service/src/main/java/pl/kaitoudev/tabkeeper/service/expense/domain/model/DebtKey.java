package pl.kaitoudev.tabkeeper.service.expense.domain.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record DebtKey(UUID debtorId, UUID creditorId) {}
