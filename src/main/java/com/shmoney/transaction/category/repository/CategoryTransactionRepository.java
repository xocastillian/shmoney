package com.shmoney.transaction.category.repository;

import com.shmoney.transaction.category.entity.CategoryTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.List;

public interface CategoryTransactionRepository extends JpaRepository<CategoryTransaction, Long>,
        JpaSpecificationExecutor<CategoryTransaction> {

    @EntityGraph(attributePaths = {"wallet", "category", "currency"})
    Optional<CategoryTransaction> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"currency", "category"})
    java.util.List<CategoryTransaction> findAllByUserIdAndCategoryIdInAndOccurredAtBetween(Long userId,
                                                                                           Collection<Long> categoryIds,
                                                                                           OffsetDateTime from,
                                                                                           OffsetDateTime to);

    @Query("SELECT DISTINCT ct.user.id FROM CategoryTransaction ct " +
            "WHERE ct.occurredAt BETWEEN :from AND :to")
    java.util.List<Long> findDistinctUserIdsByOccurredAtBetween(@Param("from") OffsetDateTime from,
                                                                @Param("to") OffsetDateTime to);

    @Query("SELECT DISTINCT ct.occurredAt FROM CategoryTransaction ct " +
            "WHERE ct.category.id = :categoryId AND ct.user.id = :userId")
    List<OffsetDateTime> findDistinctOccurredAtByCategoryIdAndUserId(@Param("categoryId") Long categoryId,
                                                                     @Param("userId") Long userId);
}
