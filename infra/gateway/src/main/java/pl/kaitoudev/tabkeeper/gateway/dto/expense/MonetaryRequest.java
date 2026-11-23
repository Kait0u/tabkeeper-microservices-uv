package pl.kaitoudev.tabkeeper.gateway.dto.expense;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/** Exact monetary input for an expense or repayment. */
public record MonetaryRequest(
    @NotNull @PositiveOrZero @Digits(integer = 15, fraction = 2) BigDecimal value,
    @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency) {}
