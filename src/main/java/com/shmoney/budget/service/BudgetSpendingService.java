package com.shmoney.budget.service;

import com.shmoney.budget.entity.Budget;
import com.shmoney.budget.entity.BudgetStatus;
import com.shmoney.budget.repository.BudgetRepository;
import com.shmoney.currency.service.ExchangeRateService;
import com.shmoney.transaction.category.entity.CategoryTransaction;
import com.shmoney.transaction.category.entity.CategoryTransactionType;
import com.shmoney.transaction.category.repository.CategoryTransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BudgetSpendingService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final BudgetRepository budgetRepository;
    private final CategoryTransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;

    public BudgetSpendingService(BudgetRepository budgetRepository,
                                 CategoryTransactionRepository transactionRepository,
                                 ExchangeRateService exchangeRateService) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.exchangeRateService = exchangeRateService;
    }

    public void handleTransactionCreated(CategoryTransaction transaction) {
        TransactionSnapshot snapshot = TransactionSnapshot.from(transaction);
        applyDelta(snapshot, true);
    }

    public void handleTransactionDeleted(CategoryTransaction transaction) {
        TransactionSnapshot snapshot = TransactionSnapshot.from(transaction);
        applyDelta(snapshot, false);
    }

    public void handleTransactionUpdated(TransactionSnapshot before, TransactionSnapshot after) {
        if (before != null) {
            applyDelta(before, false);
        }
        if (after != null) {
            applyDelta(after, true);
        }
    }

    public void recalculate(Budget budget) {
        if (budget.getCategories().isEmpty()) {
            return;
        }
        List<Long> categoryIds = budget.getCategories().stream()
                .map(category -> category.getId())
                .toList();
        List<CategoryTransaction> transactions = transactionRepository
                .findAllByUserIdAndCategoryIdInAndOccurredAtBetween(
                        budget.getOwner().getId(),
                        categoryIds,
                        budget.getPeriodStart(),
                        budget.getPeriodEnd()
                );
        BigDecimal total = BigDecimal.ZERO;
        for (CategoryTransaction tx : transactions) {
            if (tx.getType() != CategoryTransactionType.EXPENSE) {
                continue;
            }
            BigDecimal amount = convert(tx.getAmount(), tx.getCurrency().getCode(), budget.getCurrencyCode());
            total = total.add(amount);
        }
        budget.setSpentAmount(total);
        budget.setPercentSpent(calculatePercent(total, budget.getAmountLimit()));
        budgetRepository.save(budget);
    }

    private void applyDelta(TransactionSnapshot snapshot, boolean addition) {
        if (snapshot == null || snapshot.type() != CategoryTransactionType.EXPENSE) {
            return;
        }
        List<Budget> budgets = budgetRepository.findActiveBudgetsForCategory(
                snapshot.userId(),
                snapshot.categoryId(),
                snapshot.occurredAt()
        );
        if (budgets.isEmpty()) {
            return;
        }
        for (Budget budget : budgets) {
            BigDecimal converted = convert(snapshot.amount(), snapshot.currencyCode(), budget.getCurrencyCode());
            BigDecimal newSpent = addition
                    ? budget.getSpentAmount().add(converted)
                    : budget.getSpentAmount().subtract(converted);
            if (newSpent.compareTo(BigDecimal.ZERO) < 0) {
                newSpent = BigDecimal.ZERO;
            }
            budget.setSpentAmount(newSpent);
            budget.setPercentSpent(calculatePercent(newSpent, budget.getAmountLimit()));
            budgetRepository.save(budget);
        }
    }

    private BigDecimal convert(BigDecimal amount, String from, String to) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (from.equalsIgnoreCase(to)) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }
        return exchangeRateService.convert(amount, from, to);
    }

    private BigDecimal calculatePercent(BigDecimal spent, BigDecimal limit) {
        if (spent == null || limit == null || limit.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return spent.divide(limit, 4, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public record TransactionSnapshot(Long userId,
                                      Long categoryId,
                                      BigDecimal amount,
                                      String currencyCode,
                                      OffsetDateTime occurredAt,
                                      CategoryTransactionType type) {

        public static TransactionSnapshot from(CategoryTransaction transaction) {
            if (transaction == null) {
                return null;
            }
            return new TransactionSnapshot(
                    transaction.getUser().getId(),
                    transaction.getCategory().getId(),
                    transaction.getAmount(),
                    transaction.getCurrency().getCode(),
                    transaction.getOccurredAt(),
                    transaction.getType()
            );
        }
    }
}
