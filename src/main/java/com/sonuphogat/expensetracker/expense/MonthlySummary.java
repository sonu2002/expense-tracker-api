package com.sonuphogat.expensetracker.expense;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/** Spending for one month, broken down by category (largest first). */
public record MonthlySummary(
        YearMonth month,
        BigDecimal total,
        long numberOfExpenses,
        List<CategoryTotal> byCategory) {

    public record CategoryTotal(Category category, BigDecimal total, long count, double percentage) {
    }
}
