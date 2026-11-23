package pl.kaitoudev.tabkeeper.gateway.domain.expense;

import java.util.UUID;

public record Allocation(UUID userId, Monetary amount) {}
