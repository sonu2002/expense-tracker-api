package com.sonuphogat.expensetracker.common;

import com.sonuphogat.expensetracker.expense.Category;
import com.sonuphogat.expensetracker.expense.Expense;
import com.sonuphogat.expensetracker.expense.ExpenseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Inserts sample data when started with the "demo" profile, so the API can be explored immediately. */
@Component
@Profile("demo")
public class DemoDataLoader implements CommandLineRunner {

    private final ExpenseRepository repository;

    public DemoDataLoader(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now();
        repository.saveAll(List.of(
                new Expense("Monthly rent", new BigDecimal("650.00"), Category.RENT, today.withDayOfMonth(1), "WG room"),
                new Expense("Deutschlandticket", new BigDecimal("58.00"), Category.TRANSPORT, today.withDayOfMonth(1), null),
                new Expense("Weekly groceries", new BigDecimal("54.90"), Category.FOOD, today.minusDays(2), "REWE"),
                new Expense("Döner with friends", new BigDecimal("8.50"), Category.FOOD, today.minusDays(1), null),
                new Expense("Electricity", new BigDecimal("45.00"), Category.UTILITIES, today.withDayOfMonth(1), null),
                new Expense("Python course book", new BigDecimal("39.99"), Category.EDUCATION, today.minusDays(5), null),
                new Expense("Cinema", new BigDecimal("13.50"), Category.ENTERTAINMENT, today.minusDays(3), null)
        ));
    }
}
