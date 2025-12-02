package com.shmoney.budget.service;

import com.shmoney.budget.entity.BudgetPeriodType;
import com.shmoney.budget.exception.InvalidBudgetException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import org.springframework.stereotype.Component;

@Component
public class BudgetPeriodCalculator {

    public PeriodRange resolve(BudgetPeriodType periodType,
                               OffsetDateTime requestedStart,
                               OffsetDateTime requestedEnd) {
        OffsetDateTime base = requestedStart == null ? OffsetDateTime.now() : requestedStart;
        return switch (periodType) {
            case MONTH -> monthRange(base);
            case WEEK -> weekRange(base);
            case YEAR -> yearRange(base);
            case CUSTOM -> customRange(requestedStart, requestedEnd);
        };
    }

    public PeriodRange nextRange(BudgetPeriodType periodType, OffsetDateTime currentStart) {
        if (periodType == BudgetPeriodType.CUSTOM) {
            throw new InvalidBudgetException("Нельзя построить следующий период для кастомного бюджета");
        }
        OffsetDateTime base = currentStart == null ? OffsetDateTime.now() : currentStart;
        return switch (periodType) {
            case MONTH -> monthRange(base.plusMonths(1));
            case WEEK -> weekRange(base.plusWeeks(1));
            case YEAR -> yearRange(base.plusYears(1));
            case CUSTOM -> throw new InvalidBudgetException("Недопустимый тип периода");
        };
    }

    private PeriodRange monthRange(OffsetDateTime base) {
        LocalDate date = base.toLocalDate();
        LocalDate first = date.withDayOfMonth(1);
        LocalDate last = date.withDayOfMonth(date.lengthOfMonth());
        return new PeriodRange(startOfDay(first, base), endOfDay(last, base));
    }

    private PeriodRange weekRange(OffsetDateTime base) {
        LocalDate date = base.toLocalDate();
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        return new PeriodRange(startOfDay(monday, base), endOfDay(sunday, base));
    }

    private PeriodRange yearRange(OffsetDateTime base) {
        LocalDate date = base.toLocalDate();
        LocalDate first = LocalDate.of(date.getYear(), 1, 1);
        LocalDate last = LocalDate.of(date.getYear(), 12, 31);
        return new PeriodRange(startOfDay(first, base), endOfDay(last, base));
    }

    private PeriodRange customRange(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null) {
            throw new InvalidBudgetException("Для кастомного периода нужно указать даты начала и конца");
        }
        if (end.isBefore(start)) {
            throw new InvalidBudgetException("Дата окончания не может быть раньше даты начала");
        }
        return new PeriodRange(start, end);
    }

    private OffsetDateTime startOfDay(LocalDate date, OffsetDateTime reference) {
        return date.atStartOfDay(reference.getOffset()).toOffsetDateTime();
    }

    private OffsetDateTime endOfDay(LocalDate date, OffsetDateTime reference) {
        return date.atTime(23, 59, 59, 999_000_000).atOffset(reference.getOffset());
    }

    public record PeriodRange(OffsetDateTime start, OffsetDateTime end) {
    }
}
