package com.shmoney.budget.service;

import com.shmoney.budget.dto.BudgetCreateRequest;
import com.shmoney.budget.dto.BudgetFilter;
import com.shmoney.budget.dto.BudgetResponse;
import com.shmoney.budget.dto.BudgetUpdateRequest;
import com.shmoney.budget.entity.Budget;
import com.shmoney.budget.entity.BudgetPeriodType;
import com.shmoney.budget.entity.BudgetStatus;
import com.shmoney.budget.entity.BudgetType;
import com.shmoney.budget.exception.BudgetNotFoundException;
import com.shmoney.budget.exception.InvalidBudgetException;
import com.shmoney.budget.repository.BudgetRepository;
import com.shmoney.category.entity.Category;
import com.shmoney.category.repository.CategoryRepository;
import com.shmoney.currency.service.CurrencyService;
import com.shmoney.user.entity.User;
import com.shmoney.user.service.UserService;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final CurrencyService currencyService;
    private final BudgetPeriodCalculator periodCalculator;
    private final BudgetSpendingService budgetSpendingService;

    public BudgetService(BudgetRepository budgetRepository,
                         UserService userService,
                         CategoryRepository categoryRepository,
                         CurrencyService currencyService,
                         BudgetPeriodCalculator periodCalculator,
                         BudgetSpendingService budgetSpendingService) {
        this.budgetRepository = budgetRepository;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.currencyService = currencyService;
        this.periodCalculator = periodCalculator;
        this.budgetSpendingService = budgetSpendingService;
    }

    public BudgetResponse create(Long ownerId, BudgetCreateRequest request) {
        User owner = userService.getById(ownerId);
        Set<Category> categories = loadCategories(ownerId, request.categoryIds());
        if (categories.isEmpty()) {
            throw new InvalidBudgetException("Список категорий не может быть пустым");
        }
        currencyService.getActiveByCode(request.currencyCode());
        var range = periodCalculator.resolve(request.periodType(), request.periodStart(), request.periodEnd());

        Budget budget = new Budget();
        budget.setOwner(owner);
        budget.setName(request.name().trim());
        budget.setPeriodType(request.periodType());
        budget.setPeriodStart(range.start());
        budget.setPeriodEnd(range.end());
        budget.setBudgetType(request.budgetType());
        budget.setCurrencyCode(request.currencyCode().toUpperCase());
        budget.setAmountLimit(request.amountLimit());
        budget.setSpentAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        budget.setPercentSpent(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        budget.setStatus(BudgetStatus.ACTIVE);
        budget.setCategories(categories);

        Budget saved = budgetRepository.save(budget);
        budgetSpendingService.recalculate(saved);
        return toResponse(saved);
    }

    public List<BudgetResponse> list(Long ownerId, BudgetFilter filter) {
        refreshBudgets(ownerId);
        Specification<Budget> specification = buildSpecification(ownerId, filter);
        return budgetRepository.findAll(specification).stream()
                .sorted((a, b) -> {
                    int typeOrder = Integer.compare(periodOrder(a.getPeriodType()), periodOrder(b.getPeriodType()));
                    if (typeOrder != 0) {
                        return typeOrder;
                    }
                    return a.getCreatedAt().compareTo(b.getCreatedAt());
                })
                .map(this::toResponse)
                .toList();
    }

    public BudgetResponse get(Long ownerId, Long budgetId) {
        refreshBudgets(ownerId);
        Budget budget = budgetRepository.findByIdAndOwnerId(budgetId, ownerId)
                .orElseThrow(() -> new BudgetNotFoundException(budgetId));
        return toResponse(budget);
    }

    public BudgetResponse update(Long ownerId, Long budgetId, BudgetUpdateRequest request) {
        Budget budget = budgetRepository.findByIdAndOwnerId(budgetId, ownerId)
                .orElseThrow(() -> new BudgetNotFoundException(budgetId));

        if (budget.getStatus() == BudgetStatus.CLOSED) {
            throw new InvalidBudgetException("Нельзя менять закрытый бюджет");
        }

        if (request.name() != null) {
            budget.setName(request.name().trim());
        }
        if (request.budgetType() != null) {
            budget.setBudgetType(request.budgetType());
        }

        BudgetPeriodType periodType = request.periodType() != null ? request.periodType() : budget.getPeriodType();
        OffsetDateTime requestedStart = request.periodStart() != null ? request.periodStart() : budget.getPeriodStart();
        OffsetDateTime requestedEnd = request.periodEnd() != null ? request.periodEnd() : budget.getPeriodEnd();
        if (periodType == BudgetPeriodType.CUSTOM &&
                (request.periodStart() == null || request.periodEnd() == null) &&
                budget.getPeriodType() != BudgetPeriodType.CUSTOM) {
            throw new InvalidBudgetException("Для кастомного периода укажите даты начала и конца");
        }
        if (request.periodType() != null || request.periodStart() != null || request.periodEnd() != null) {
            var range = periodCalculator.resolve(periodType, requestedStart, requestedEnd);
            budget.setPeriodType(periodType);
            budget.setPeriodStart(range.start());
            budget.setPeriodEnd(range.end());
        }

        if (request.amountLimit() != null) {
            if (request.amountLimit().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidBudgetException("Лимит бюджета должен быть больше нуля");
            }
            budget.setAmountLimit(request.amountLimit());
        }

        if (request.currencyCode() != null) {
            currencyService.getActiveByCode(request.currencyCode());
            budget.setCurrencyCode(request.currencyCode().toUpperCase());
        }

        if (request.categoryIds() != null) {
            Set<Category> categories = loadCategories(ownerId, request.categoryIds());
            if (categories.isEmpty()) {
                throw new InvalidBudgetException("Список категорий не может быть пустым");
            }
            budget.setCategories(categories);
        }

        Budget saved = budgetRepository.save(budget);
        budgetSpendingService.recalculate(saved);
        return toResponse(saved);
    }

    public BudgetResponse close(Long ownerId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndOwnerId(budgetId, ownerId)
                .orElseThrow(() -> new BudgetNotFoundException(budgetId));
        if (budget.getStatus() == BudgetStatus.CLOSED) {
            return toResponse(budget);
        }
        closeBudget(budget, OffsetDateTime.now());
        return toResponse(budget);
    }

    public void delete(Long ownerId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndOwnerId(budgetId, ownerId)
                .orElseThrow(() -> new BudgetNotFoundException(budgetId));
        budgetRepository.delete(budget);
    }

    public void refreshBudgets(Long ownerId) {
        OffsetDateTime now = OffsetDateTime.now();
        List<Budget> overdue = budgetRepository.findAllByOwnerIdAndStatusAndPeriodEndBefore(ownerId,
                BudgetStatus.ACTIVE,
                now);
        for (Budget budget : overdue) {
            closeBudget(budget, now);
            if (budget.getBudgetType() == BudgetType.RECURRING) {
                createNextBudget(budget);
            }
        }
    }

    private void createNextBudget(Budget closedBudget) {
        Budget next = new Budget();
        next.setOwner(closedBudget.getOwner());
        next.setName(closedBudget.getName());
        next.setPeriodType(closedBudget.getPeriodType());
        if (closedBudget.getPeriodType() == BudgetPeriodType.CUSTOM) {
            OffsetDateTime start = closedBudget.getPeriodStart();
            OffsetDateTime end = closedBudget.getPeriodEnd();
            if (start == null || end == null || !end.isAfter(start)) {
                throw new InvalidBudgetException("Некорректный период кастомного бюджета");
            }
            java.time.Duration duration = java.time.Duration.between(start, end);
            OffsetDateTime nextStart = end.plusSeconds(1);
            OffsetDateTime nextEnd = nextStart.plus(duration);
            next.setPeriodStart(nextStart);
            next.setPeriodEnd(nextEnd);
        } else {
            var range = periodCalculator.nextRange(closedBudget.getPeriodType(), closedBudget.getPeriodStart());
            next.setPeriodStart(range.start());
            next.setPeriodEnd(range.end());
        }
        next.setBudgetType(closedBudget.getBudgetType());
        next.setCurrencyCode(closedBudget.getCurrencyCode());
        next.setAmountLimit(closedBudget.getAmountLimit());
        next.setSpentAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        next.setPercentSpent(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        next.setStatus(BudgetStatus.ACTIVE);
        next.setCategories(new HashSet<>(closedBudget.getCategories()));
        budgetRepository.save(next);
    }

    private void closeBudget(Budget budget, OffsetDateTime closedAt) {
        budget.setStatus(BudgetStatus.CLOSED);
        budget.setClosedAt(closedAt);
        budgetRepository.save(budget);
    }

    private Set<Category> loadCategories(Long ownerId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        List<Category> categories = categoryRepository.findAllByOwnerIdAndIdIn(ownerId, ids);
        if (categories.size() != ids.stream().distinct().count()) {
            throw new InvalidBudgetException("Некоторые категории не найдены или не принадлежат пользователю");
        }
        return new HashSet<>(categories);
    }

    private Specification<Budget> buildSpecification(Long ownerId, BudgetFilter filter) {
        return (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("owner").get("id"), ownerId);
            if (filter != null) {
                if (filter.status() != null) {
                    predicate = cb.and(predicate, cb.equal(root.get("status"), filter.status()));
                }
                if (filter.periodType() != null) {
                    predicate = cb.and(predicate, cb.equal(root.get("periodType"), filter.periodType()));
                }
                if (filter.budgetType() != null) {
                    predicate = cb.and(predicate, cb.equal(root.get("budgetType"), filter.budgetType()));
                }
                if (filter.from() != null) {
                    predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("periodEnd"), filter.from()));
                }
                if (filter.to() != null) {
                    predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("periodStart"), filter.to()));
                }
            }
            query.distinct(true);
            return predicate;
        };
    }

    private int periodOrder(BudgetPeriodType type) {
        return switch (type) {
            case CUSTOM -> 0;
            case WEEK -> 1;
            case MONTH -> 2;
            case YEAR -> 3;
        };
    }

    private BudgetResponse toResponse(Budget budget) {
        List<Long> categoryIds = budget.getCategories().stream()
                .map(Category::getId)
                .sorted()
                .toList();
        return new BudgetResponse(
                budget.getId(),
                budget.getName(),
                budget.getPeriodType(),
                budget.getPeriodStart(),
                budget.getPeriodEnd(),
                budget.getBudgetType(),
                budget.getCurrencyCode(),
                budget.getAmountLimit(),
                budget.getSpentAmount(),
                budget.getPercentSpent(),
                budget.getStatus(),
                budget.getClosedAt(),
                budget.getCreatedAt(),
                budget.getUpdatedAt(),
                categoryIds
        );
    }
}
