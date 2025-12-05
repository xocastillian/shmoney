package com.shmoney.analytics.service;

import com.shmoney.analytics.dto.AnalyticsPeriod;
import com.shmoney.analytics.dto.AnalyticsResponse;
import com.shmoney.analytics.dto.CategoryAnalyticsItem;
import com.shmoney.analytics.entity.MonthlyAnalytics;
import com.shmoney.analytics.model.CategoryBreakdown;
import com.shmoney.analytics.repository.MonthlyAnalyticsRepository;
import com.shmoney.currency.service.ExchangeRateService;
import com.shmoney.settings.entity.AppSettings;
import com.shmoney.settings.service.AppSettingsProvider;
import com.shmoney.transaction.category.entity.CategoryTransaction;
import com.shmoney.transaction.category.entity.CategoryTransactionType;
import com.shmoney.transaction.category.repository.CategoryTransactionRepository;
import com.shmoney.transaction.category.repository.CategoryTransactionSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnalyticsService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int AMOUNT_SCALE = 2;

    private final MonthlyAnalyticsRepository monthlyAnalyticsRepository;
    private final CategoryTransactionRepository categoryTransactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final AppSettingsProvider appSettingsProvider;

    public AnalyticsService(MonthlyAnalyticsRepository monthlyAnalyticsRepository,
                            CategoryTransactionRepository categoryTransactionRepository,
                            ExchangeRateService exchangeRateService,
                            AppSettingsProvider appSettingsProvider) {
        this.monthlyAnalyticsRepository = monthlyAnalyticsRepository;
        this.categoryTransactionRepository = categoryTransactionRepository;
        this.exchangeRateService = exchangeRateService;
        this.appSettingsProvider = appSettingsProvider;
    }

    public AnalyticsResponse getAnalytics(Long userId,
                                          OffsetDateTime from,
                                          OffsetDateTime to,
                                          List<Long> categoryIds) {
        AnalyticsPeriod period = resolvePeriod(from, to);
        String targetCurrency = resolveMainCurrency();
        boolean hasFilters = from != null || to != null || (categoryIds != null && !categoryIds.isEmpty());
        MonthlyAnalytics storedSummary = null;

        if (!hasFilters) {
            storedSummary = monthlyAnalyticsRepository
                    .findByUserIdAndPeriodStart(userId, period.from())
                    .orElse(null);
        }

        if (storedSummary != null) {
            boolean countsMissing = storedSummary.getExpenseBreakdown().stream()
                    .anyMatch(entry -> entry.transactionCount() <= 0
                            && entry.amount() != null
                            && entry.amount().compareTo(BigDecimal.ZERO) > 0);
            if (countsMissing) {
                monthlyAnalyticsRepository.deleteByUserIdAndPeriodStart(userId, period.from());
                storedSummary = null;
            }
        }

        if (storedSummary != null) {
            List<CategoryAnalyticsItem> categories = buildCategoryItems(
                    storedSummary.getExpenseBreakdown(),
                    storedSummary.getTotalExpense()
            );
            long totalExpenseTransactions = categories.stream()
                    .mapToLong(CategoryAnalyticsItem::transactionCount)
                    .sum();
            List<CategoryAnalyticsItem> top = categories.stream()
                    .sorted(Comparator.comparing(CategoryAnalyticsItem::amount).reversed())
                    .limit(3)
                    .toList();
            return new AnalyticsResponse(
                    new AnalyticsPeriod(storedSummary.getPeriodStart(), storedSummary.getPeriodEnd()),
                    storedSummary.getCurrencyCode(),
                    storedSummary.getTotalExpense(),
                    storedSummary.getTotalIncome(),
                    storedSummary.getCashFlowAmount(),
                    storedSummary.getCashFlowPercent(),
                    totalExpenseTransactions,
                    categories,
                    top
            );
        }

        ComputationResult result = compute(userId, period, categoryIds, targetCurrency);
        List<CategoryAnalyticsItem> categories = buildCategoryItems(result.expenseBreakdown(), result.totalExpense());
        List<CategoryAnalyticsItem> topCategories = categories.stream()
                .sorted(Comparator.comparing(CategoryAnalyticsItem::amount).reversed())
                .limit(3)
                .toList();
        BigDecimal cashFlowAmount = result.totalIncome().subtract(result.totalExpense());
        BigDecimal cashFlowPercent = calculateCashFlowPercent(result.totalIncome(), result.totalExpense());

        if (!hasFilters && result.hasTransactions()) {
            monthlyAnalyticsRepository.deleteByUserIdAndPeriodStart(userId, period.from());
            MonthlyAnalytics entity = new MonthlyAnalytics();
            entity.setUserId(userId);
            entity.setPeriodStart(period.from());
            entity.setPeriodEnd(period.to());
            entity.setCurrencyCode(targetCurrency);
            entity.setTotalExpense(result.totalExpense());
            entity.setTotalIncome(result.totalIncome());
            entity.setCashFlowAmount(cashFlowAmount);
            entity.setCashFlowPercent(cashFlowPercent);
            entity.setExpenseBreakdown(result.expenseBreakdown());
            monthlyAnalyticsRepository.save(entity);
        }

        return new AnalyticsResponse(
                period,
                targetCurrency,
                result.totalExpense(),
                result.totalIncome(),
                cashFlowAmount,
                cashFlowPercent,
                result.expenseTransactionCount(),
                categories,
                topCategories
        );
    }

    public void recalculateAllSummaries(String newCurrency) {
        String target = newCurrency == null ? resolveMainCurrency() : newCurrency.toUpperCase(Locale.ROOT);
        List<MonthlyAnalytics> summaries = monthlyAnalyticsRepository.findAll();
        for (MonthlyAnalytics summary : summaries) {
            String current = summary.getCurrencyCode();
            if (current != null && current.equalsIgnoreCase(target)) {
                continue;
            }
            BigDecimal expense = convert(summary.getTotalExpense(), current, target);
            BigDecimal income = convert(summary.getTotalIncome(), current, target);
            BigDecimal cashFlow = income.subtract(expense);
            summary.setTotalExpense(expense);
            summary.setTotalIncome(income);
            summary.setCashFlowAmount(cashFlow);
            summary.setCashFlowPercent(calculateCashFlowPercent(income, expense));
            summary.setCurrencyCode(target);
            List<CategoryBreakdown> updated = summary.getExpenseBreakdown().stream()
                    .map(entry -> new CategoryBreakdown(
                            entry.categoryId(),
                            entry.categoryName(),
                            entry.categoryColor(),
                            entry.categoryIcon(),
                            convert(entry.amount(), current, target),
                            entry.transactionCount()
                    ))
                    .toList();
            summary.setExpenseBreakdown(updated);
        }
        monthlyAnalyticsRepository.saveAll(summaries);
    }

    @Scheduled(cron = "0 5 0 1 * *")
    public void buildPreviousMonthSnapshots() {
        AnalyticsPeriod previousMonth = resolvePreviousMonth();
        List<Long> userIds = categoryTransactionRepository
                .findDistinctUserIdsByOccurredAtBetween(previousMonth.from(), previousMonth.to());
        if (userIds.isEmpty()) {
            return;
        }
        String currency = resolveMainCurrency();
        for (Long userId : userIds) {
            Specification<CategoryTransaction> spec = Specification
                    .where(CategoryTransactionSpecifications.belongsToUser(userId))
                    .and(CategoryTransactionSpecifications.occurredAfter(previousMonth.from()))
                    .and(CategoryTransactionSpecifications.occurredBefore(previousMonth.to()));
            List<CategoryTransaction> transactions = categoryTransactionRepository.findAll(spec);
            if (transactions.isEmpty()) {
                continue;
            }
            if (monthlyAnalyticsRepository.findByUserIdAndPeriodStart(userId, previousMonth.from()).isPresent()) {
                continue;
            }
            ComputationResult result = computeFromTransactions(transactions, currency);
            if (!result.hasTransactions()) {
                continue;
            }
            MonthlyAnalytics entity = new MonthlyAnalytics();
            entity.setUserId(userId);
            entity.setPeriodStart(previousMonth.from());
            entity.setPeriodEnd(previousMonth.to());
            entity.setCurrencyCode(currency);
            entity.setTotalExpense(result.totalExpense());
            entity.setTotalIncome(result.totalIncome());
            BigDecimal cashFlowAmount = result.totalIncome().subtract(result.totalExpense());
            entity.setCashFlowAmount(cashFlowAmount);
            entity.setCashFlowPercent(calculateCashFlowPercent(result.totalIncome(), result.totalExpense()));
            entity.setExpenseBreakdown(result.expenseBreakdown());
            monthlyAnalyticsRepository.save(entity);
        }
    }

    private ComputationResult compute(Long userId,
                                      AnalyticsPeriod period,
                                      List<Long> categoryIds,
                                      String targetCurrency) {
        Specification<CategoryTransaction> specification = Specification
                .where(CategoryTransactionSpecifications.belongsToUser(userId))
                .and(CategoryTransactionSpecifications.occurredAfter(period.from()))
                .and(CategoryTransactionSpecifications.occurredBefore(period.to()))
                .and(CategoryTransactionSpecifications.hasCategories(categoryIds));
        List<CategoryTransaction> transactions = categoryTransactionRepository.findAll(specification);
        return computeFromTransactions(transactions, targetCurrency);
    }

    private ComputationResult computeFromTransactions(List<CategoryTransaction> transactions,
                                                      String targetCurrency) {
        BigDecimal totalExpense = BigDecimal.ZERO.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        BigDecimal totalIncome = BigDecimal.ZERO.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        Map<Long, CategoryAccumulator> categoryTotals = new HashMap<>();
        long expenseCount = 0L;

        for (CategoryTransaction transaction : transactions) {
            BigDecimal amount = normalize(convert(transaction.getAmount(),
                    transaction.getCurrency().getCode(),
                    targetCurrency));
            if (transaction.getType() == CategoryTransactionType.EXPENSE) {
                totalExpense = totalExpense.add(amount);
                var category = transaction.getCategory();
                if (category == null) {
                    continue;
                }
                CategoryAccumulator accumulator = categoryTotals.computeIfAbsent(
                        category.getId(),
                        id -> new CategoryAccumulator(
                                category.getId(),
                                category.getName(),
                                category.getColor(),
                                category.getIcon()
                        )
                );
                accumulator.add(amount);
                expenseCount++;
            } else if (transaction.getType() == CategoryTransactionType.INCOME) {
                totalIncome = totalIncome.add(amount);
            }
        }

        List<CategoryBreakdown> breakdown = categoryTotals.values().stream()
                .map(CategoryAccumulator::toBreakdown)
                .sorted(Comparator.comparing(CategoryBreakdown::amount).reversed())
                .toList();

        return new ComputationResult(totalExpense, totalIncome, breakdown, expenseCount);
    }

    private BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        }
        if (fromCurrency == null || toCurrency == null || fromCurrency.equalsIgnoreCase(toCurrency)) {
            return normalize(amount);
        }
        return normalize(exchangeRateService.convert(amount, fromCurrency, toCurrency));
    }

    private BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        }
        return value.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCashFlowPercent(BigDecimal income, BigDecimal expense) {
        if (income == null || income.compareTo(BigDecimal.ZERO) == 0) {
            if (expense == null || expense.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
            return BigDecimal.valueOf(-100).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal profit = income.subtract(expense);
        return profit.divide(income, 4, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<CategoryAnalyticsItem> buildCategoryItems(List<CategoryBreakdown> breakdown, BigDecimal totalExpense) {
        BigDecimal denominator = totalExpense == null ? BigDecimal.ZERO : totalExpense;
        return breakdown.stream()
                .map(entry -> {
                    BigDecimal percent = (denominator.compareTo(BigDecimal.ZERO) == 0)
                            ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                            : entry.amount().divide(denominator, 4, RoundingMode.HALF_UP)
                            .multiply(ONE_HUNDRED)
                            .setScale(2, RoundingMode.HALF_UP);
                    return new CategoryAnalyticsItem(
                            entry.categoryId(),
                            entry.categoryName(),
                            entry.categoryColor(),
                            entry.categoryIcon(),
                            entry.amount(),
                            percent,
                            entry.transactionCount()
                    );
                })
                .sorted(Comparator.comparing(CategoryAnalyticsItem::amount).reversed())
                .collect(Collectors.toList());
    }

    private AnalyticsPeriod resolvePeriod(OffsetDateTime from, OffsetDateTime to) {
        if ((from == null) != (to == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Необходимо указать обе даты диапазона или ни одной");
        }
        if (from != null && to != null) {
            if (to.isBefore(from)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата окончания раньше даты начала");
            }
            return new AnalyticsPeriod(from, to);
        }
        OffsetDateTime start = monthStart(OffsetDateTime.now());
        OffsetDateTime end = monthEnd(start);
        return new AnalyticsPeriod(start, end);
    }

    private AnalyticsPeriod resolvePreviousMonth() {
        OffsetDateTime currentStart = monthStart(OffsetDateTime.now());
        OffsetDateTime start = currentStart.minusMonths(1);
        OffsetDateTime end = monthEnd(start);
        return new AnalyticsPeriod(start, end);
    }

    public void invalidateMonth(Long userId, OffsetDateTime occurredAt) {
        if (userId == null || occurredAt == null) {
            return;
        }
        OffsetDateTime start = monthStart(occurredAt);
        monthlyAnalyticsRepository.deleteByUserIdAndPeriodStart(userId, start);
    }

    private String resolveMainCurrency() {
        AppSettings settings = appSettingsProvider.getOrCreate();
        return settings.getMainCurrency() == null
                ? "KZT"
                : settings.getMainCurrency().toUpperCase(Locale.ROOT);
    }

    private OffsetDateTime monthStart(OffsetDateTime timestamp) {
        OffsetDateTime base = timestamp == null ? OffsetDateTime.now() : timestamp;
        return base.withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    private OffsetDateTime monthEnd(OffsetDateTime start) {
        return start.plusMonths(1).minusNanos(1);
    }

    private record ComputationResult(BigDecimal totalExpense,
                                     BigDecimal totalIncome,
                                     List<CategoryBreakdown> expenseBreakdown,
                                     long expenseTransactionCount) {
        boolean hasTransactions() {
            return (totalExpense != null && totalExpense.compareTo(BigDecimal.ZERO) > 0)
                    || (totalIncome != null && totalIncome.compareTo(BigDecimal.ZERO) > 0)
                    || (expenseBreakdown != null && !expenseBreakdown.isEmpty());
        }
    }

    private static class CategoryAccumulator {
        private final Long categoryId;
        private final String name;
        private final String color;
        private final String icon;
        private BigDecimal amount = BigDecimal.ZERO.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        private long count = 0L;

        private CategoryAccumulator(Long categoryId, String name, String color, String icon) {
            this.categoryId = categoryId;
            this.name = name;
            this.color = color;
            this.icon = icon;
        }

        void add(BigDecimal value) {
            if (value == null) {
                return;
            }
            amount = amount.add(value);
            count++;
        }

        CategoryBreakdown toBreakdown() {
            return new CategoryBreakdown(categoryId, name, color, icon, amount, count);
        }
    }
}
