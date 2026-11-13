package com.sonuphogat.expensetracker;

import com.sonuphogat.expensetracker.common.NotFoundException;
import com.sonuphogat.expensetracker.expense.Category;
import com.sonuphogat.expensetracker.expense.ExpenseRepository;
import com.sonuphogat.expensetracker.expense.ExpenseService;
import com.sonuphogat.expensetracker.expense.MonthlySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpenseServiceTest {

    private ExpenseRepository repository;
    private ExpenseService service;

    @BeforeEach
    void setUp() {
        repository = mock(ExpenseRepository.class);
        service = new ExpenseService(repository);
    }

    @Test
    void monthlySummaryCalculatesTotalsAndPercentages() {
        YearMonth month = YearMonth.of(2026, 11);
        when(repository.totalsByCategory(month.atDay(1), month.atEndOfMonth())).thenReturn(List.of(
                new Object[]{Category.RENT, new BigDecimal("600.00"), 1L},
                new Object[]{Category.FOOD, new BigDecimal("150.00"), 3L},
                new Object[]{Category.TRANSPORT, new BigDecimal("50.00"), 1L}));

        MonthlySummary summary = service.monthlySummary(month);

        assertThat(summary.total()).isEqualByComparingTo("800.00");
        assertThat(summary.numberOfExpenses()).isEqualTo(5);
        assertThat(summary.byCategory()).extracting(MonthlySummary.CategoryTotal::percentage)
                .containsExactly(75.0, 18.8, 6.3);
    }

    @Test
    void monthlySummaryOfEmptyMonthIsZero() {
        when(repository.totalsByCategory(any(), any())).thenReturn(List.of());

        MonthlySummary summary = service.monthlySummary(YearMonth.of(2026, 1));

        assertThat(summary.total()).isEqualByComparingTo("0");
        assertThat(summary.byCategory()).isEmpty();
    }

    @Test
    void getUnknownExpenseThrowsNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void searchRejectsInvertedDateRange() {
        assertThatThrownBy(() -> service.search(null, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1), Pageable.unpaged()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
