package com.shmoney.transaction.category.repository;

import com.shmoney.transaction.category.entity.CategoryTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;

public interface CategoryTransactionRepository extends JpaRepository<CategoryTransaction, Long>,
        JpaSpecificationExecutor<CategoryTransaction> {

    @EntityGraph(attributePaths = {"wallet", "category", "currency"})
    Optional<CategoryTransaction> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"currency", "category"})
    java.util.List<CategoryTransaction> findAllByUserIdAndCategoryIdInAndOccurredAtBetween(Long userId,
                                                                                           Collection<Long> categoryIds,
                                                                                           OffsetDateTime from,
                                                                                           OffsetDateTime to);
}
