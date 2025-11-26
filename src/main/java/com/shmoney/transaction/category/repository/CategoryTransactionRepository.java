package com.shmoney.transaction.category.repository;

import com.shmoney.transaction.category.entity.CategoryTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CategoryTransactionRepository extends JpaRepository<CategoryTransaction, Long>,
        JpaSpecificationExecutor<CategoryTransaction> {

    @EntityGraph(attributePaths = {"wallet", "category", "subcategory", "currency"})
    Optional<CategoryTransaction> findByIdAndUserId(Long id, Long userId);
}
