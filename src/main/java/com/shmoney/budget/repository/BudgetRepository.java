package com.shmoney.budget.repository;

import com.shmoney.budget.entity.Budget;
import com.shmoney.budget.entity.BudgetStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BudgetRepository extends JpaRepository<Budget, Long>, JpaSpecificationExecutor<Budget> {

    List<Budget> findAllByOwnerIdAndStatusAndPeriodEndBefore(Long ownerId, BudgetStatus status, OffsetDateTime before);

    List<Budget> findAllByOwnerId(Long ownerId);

    Optional<Budget> findByIdAndOwnerId(Long id, Long ownerId);

    @Query("SELECT DISTINCT b FROM Budget b JOIN b.categories c " +
            "WHERE b.owner.id = :ownerId AND c.id = :categoryId AND b.status = com.shmoney.budget.entity.BudgetStatus.ACTIVE " +
            "AND :occurredAt BETWEEN b.periodStart AND b.periodEnd")
    List<Budget> findActiveBudgetsForCategory(@Param("ownerId") Long ownerId,
                                              @Param("categoryId") Long categoryId,
                                              @Param("occurredAt") OffsetDateTime occurredAt);

}
