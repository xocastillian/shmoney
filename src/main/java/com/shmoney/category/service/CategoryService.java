package com.shmoney.category.service;

import com.shmoney.analytics.service.AnalyticsService;
import com.shmoney.category.entity.Category;
import com.shmoney.category.entity.CategoryStatus;
import com.shmoney.category.exception.CategoryNotFoundException;
import com.shmoney.category.repository.CategoryRepository;
import com.shmoney.transaction.category.repository.CategoryTransactionRepository;
import com.shmoney.user.entity.User;
import com.shmoney.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final CategoryTransactionRepository categoryTransactionRepository;
    private final AnalyticsService analyticsService;

    public CategoryService(CategoryRepository categoryRepository,
                           UserService userService,
                           CategoryTransactionRepository categoryTransactionRepository,
                           AnalyticsService analyticsService) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
        this.categoryTransactionRepository = categoryTransactionRepository;
        this.analyticsService = analyticsService;
    }

    public Category create(Long ownerId, Category category) {
        User owner = userService.getById(ownerId);
        category.setOwner(owner);
        category.setStatus(CategoryStatus.ACTIVE);
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> getAll(Long ownerId) {
        return categoryRepository.findAllByOwnerIdOrderByNameAsc(ownerId);
    }

    @Transactional(readOnly = true)
    public Category getOwnedCategory(Long id, Long ownerId) {
        return categoryRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    public Category update(Category category) {
        Category saved = categoryRepository.save(category);
        invalidateAnalytics(saved);
        return saved;
    }

    public Category updateStatus(Category category, CategoryStatus status) {
        if (status == null) {
            return category;
        }
        category.setStatus(status);
        Category saved = categoryRepository.save(category);
        invalidateAnalytics(saved);
        return saved;
    }

    private void invalidateAnalytics(Category category) {
        if (category == null || category.getId() == null || category.getOwner() == null) {
            return;
        }
        Long userId = category.getOwner().getId();
        if (userId == null) {
            return;
        }
        List<OffsetDateTime> timestamps = categoryTransactionRepository
                .findDistinctOccurredAtByCategoryIdAndUserId(category.getId(), userId);
        if (timestamps == null || timestamps.isEmpty()) {
            return;
        }
        Set<OffsetDateTime> months = timestamps.stream()
                .filter(Objects::nonNull)
                .map(this::monthStart)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        months.forEach(month -> analyticsService.invalidateMonth(userId, month));
    }

    private OffsetDateTime monthStart(OffsetDateTime timestamp) {
        OffsetDateTime base = timestamp == null ? OffsetDateTime.now() : timestamp;
        return base.withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }
}
