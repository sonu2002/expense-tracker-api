package com.sonuphogat.expensetracker.expense;

import com.sonuphogat.expensetracker.common.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExpenseService {

    private static final LocalDate MIN_DATE = LocalDate.of(1900, 1, 1);
    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        Expense saved = repository.save(new Expense(request.title().trim(), request.amount(), request.category(),
                request.spentOn(), request.note()));
        return ExpenseResponse.from(saved);
    }

    public ExpenseResponse get(Long id) {
        return ExpenseResponse.from(find(id));
    }

    public Page<ExpenseResponse> search(Category category, LocalDate from, LocalDate to, Pageable pageable) {
        LocalDate start = from != null ? from : MIN_DATE;
        LocalDate end = to != null ? to : MAX_DATE;
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("'from' must be on or before 'to'");
        }
        return repository.search(category, start, end, pageable).map(ExpenseResponse::from);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense expense = find(id);
        expense.update(request.title().trim(), request.amount(), request.category(), request.spentOn(), request.note());
        return ExpenseResponse.from(repository.save(expense));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    public MonthlySummary monthlySummary(YearMonth month) {
        List<Object[]> rows = repository.totalsByCategory(month.atDay(1), month.atEndOfMonth());

        BigDecimal total = rows.stream().map(r -> (BigDecimal) r[1]).reduce(BigDecimal.ZERO, BigDecimal::add);
        long count = rows.stream().mapToLong(r -> (Long) r[2]).sum();

        List<MonthlySummary.CategoryTotal> byCategory = rows.stream()
                .map(r -> new MonthlySummary.CategoryTotal(
                        (Category) r[0],
                        (BigDecimal) r[1],
                        (Long) r[2],
                        percentage((BigDecimal) r[1], total)))
                .toList();

        return new MonthlySummary(month, total, count, byCategory);
    }

    private static double percentage(BigDecimal part, BigDecimal total) {
        if (total.signum() == 0) {
            return 0.0;
        }
        return part.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP).doubleValue();
    }

    private Expense find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Expense " + id + " not found"));
    }
}
