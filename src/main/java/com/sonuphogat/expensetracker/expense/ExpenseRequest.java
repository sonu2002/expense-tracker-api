package com.sonuphogat.expensetracker.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Payload for creating or updating an expense. */
public record ExpenseRequest(
        @Schema(example = "Weekly groceries")
        @NotBlank @Size(max = 100) String title,

        @Schema(example = "54.90")
        @NotNull @DecimalMin(value = "0.01") @DecimalMax(value = "1000000.00") @Digits(integer = 10, fraction = 2)
        BigDecimal amount,

        @Schema(example = "FOOD")
        @NotNull Category category,

        @Schema(example = "2026-11-10")
        @NotNull @PastOrPresent LocalDate spentOn,

        @Schema(example = "REWE + Lidl")
        @Size(max = 500) String note
) {
}
