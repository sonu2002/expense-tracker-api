package com.sonuphogat.expensetracker.expense;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id, String title, BigDecimal amount, Category category, LocalDate spentOn, String note,
        Instant createdAt, Instant updatedAt) {

    static ExpenseResponse from(Expense e) {
        return new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getSpentOn(),
                e.getNote(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
