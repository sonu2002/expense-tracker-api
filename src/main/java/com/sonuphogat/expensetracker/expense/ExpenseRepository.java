package com.sonuphogat.expensetracker.expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
            SELECT e FROM Expense e
            WHERE (:category IS NULL OR e.category = :category)
              AND e.spentOn BETWEEN :from AND :to
            """)
    Page<Expense> search(@Param("category") Category category,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to,
                         Pageable pageable);

    /** Returns rows of [category, sum(amount), count] for the given date range. */
    @Query("""
            SELECT e.category, SUM(e.amount), COUNT(e)
            FROM Expense e
            WHERE e.spentOn BETWEEN :from AND :to
            GROUP BY e.category
            ORDER BY SUM(e.amount) DESC
            """)
    List<Object[]> totalsByCategory(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
