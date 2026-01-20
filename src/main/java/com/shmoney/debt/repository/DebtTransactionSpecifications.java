package com.shmoney.debt.repository;

import com.shmoney.debt.entity.DebtTransaction;
import com.shmoney.debt.entity.DebtTransactionDirection;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

public final class DebtTransactionSpecifications {
    
    public static Specification<DebtTransaction> belongsToUser(Long userId) {
        if (userId == null) return null;
        
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }
    
    public static Specification<DebtTransaction> hasCounterparty(Long counterpartyId) {
        if (counterpartyId == null) return null;
        
        return (root, query, cb) -> cb.equal(root.get("counterparty").get("id"), counterpartyId);
    }
    
    public static Specification<DebtTransaction> hasDirection(DebtTransactionDirection direction) {
        if (direction == null) return null;
        
        return (root, query, cb) -> cb.equal(root.get("direction"), direction);
    }
    
    public static Specification<DebtTransaction> occurredAfter(OffsetDateTime from) {
        if (from == null) return null;
        
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), from);
    }
    
    public static Specification<DebtTransaction> occurredBefore(OffsetDateTime to) {
        if (to == null) return null;
        
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), to);
    }
}
