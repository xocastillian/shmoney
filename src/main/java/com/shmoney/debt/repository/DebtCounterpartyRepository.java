package com.shmoney.debt.repository;

import com.shmoney.debt.entity.DebtCounterparty;
import com.shmoney.debt.entity.DebtCounterpartyStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DebtCounterpartyRepository extends JpaRepository<DebtCounterparty, Long> {

    @EntityGraph(attributePaths = {"currency"})
    List<DebtCounterparty> findAllByUserIdOrderByIdAsc(Long userId);

    @EntityGraph(attributePaths = {"currency"})
    List<DebtCounterparty> findAllByUserIdAndStatusOrderByIdAsc(Long userId, DebtCounterpartyStatus status);

    @EntityGraph(attributePaths = {"currency"})
    Optional<DebtCounterparty> findByIdAndUserId(Long id, Long userId);
}
