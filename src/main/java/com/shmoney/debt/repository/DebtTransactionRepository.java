package com.shmoney.debt.repository;

import com.shmoney.debt.entity.DebtTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface DebtTransactionRepository extends JpaRepository<DebtTransaction, Long>,
        JpaSpecificationExecutor<DebtTransaction> {

    @Override
    @EntityGraph(attributePaths = {"counterparty", "wallet", "currency"})
    Page<DebtTransaction> findAll(Specification<DebtTransaction> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"counterparty", "wallet", "currency"})
    Optional<DebtTransaction> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"currency"})
    List<DebtTransaction> findAllByCounterpartyId(Long counterpartyId);
}
