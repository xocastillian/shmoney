package com.shmoney.transaction.category.repository;

import com.shmoney.transaction.category.entity.CategoryTransaction;
import com.shmoney.transaction.category.entity.CategoryTransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.Collection;

public final class CategoryTransactionSpecifications {

    private CategoryTransactionSpecifications() {
    }

    public static Specification<CategoryTransaction> belongsToUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<CategoryTransaction> hasWallet(Long walletId) {
        return walletId == null
                ? null
                : (root, query, cb) -> cb.equal(root.get("wallet").get("id"), walletId);
    }

    public static Specification<CategoryTransaction> hasCategory(Long categoryId) {
        return categoryId == null
                ? null
                : (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<CategoryTransaction> hasCategories(Collection<Long> categoryIds) {
        return categoryIds == null || categoryIds.isEmpty()
                ? null
                : (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    public static Specification<CategoryTransaction> hasType(CategoryTransactionType type) {
        return type == null
                ? null
                : (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<CategoryTransaction> occurredAfter(OffsetDateTime from) {
        return from == null
                ? null
                : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), from);
    }

    public static Specification<CategoryTransaction> occurredBefore(OffsetDateTime to) {
        return to == null
                ? null
                : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), to);
    }
}
