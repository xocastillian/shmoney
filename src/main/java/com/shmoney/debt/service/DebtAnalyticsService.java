package com.shmoney.debt.service;

import com.shmoney.debt.dto.DebtCounterpartySummary;
import com.shmoney.debt.dto.DebtSummaryResponse;
import com.shmoney.debt.dto.DebtTransactionFilter;
import com.shmoney.debt.entity.DebtCounterparty;
import com.shmoney.debt.entity.DebtTransaction;
import com.shmoney.debt.repository.DebtCounterpartyRepository;
import com.shmoney.debt.repository.DebtTransactionRepository;
import com.shmoney.debt.repository.DebtTransactionSpecifications;
import com.shmoney.settings.service.AppSettingsProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DebtAnalyticsService {
    
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    
    private final DebtCounterpartyRepository counterpartyRepository;
    private final DebtTransactionRepository transactionRepository;
    private final AppSettingsProvider appSettingsProvider;
    
    public DebtAnalyticsService(DebtCounterpartyRepository counterpartyRepository,
                                DebtTransactionRepository transactionRepository,
                                AppSettingsProvider appSettingsProvider) {
        this.counterpartyRepository = counterpartyRepository;
        this.transactionRepository = transactionRepository;
        this.appSettingsProvider = appSettingsProvider;
    }
    
    public DebtSummaryResponse getSummary(Long userId) {
        List<DebtCounterparty> counterparties = counterpartyRepository.findAllByUserIdOrderByIdAsc(userId);
        BigDecimal totalOwed = counterparties.stream()
                .map(DebtCounterparty::getOwedToMe)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalIOwe = counterparties.stream()
                .map(DebtCounterparty::getIOwe)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<DebtCounterpartySummary> summaries = counterparties.stream()
                .map(counterparty -> new DebtCounterpartySummary(
                        counterparty.getId(),
                        counterparty.getName(),
                        counterparty.getOwedToMe(),
                        counterparty.getIOwe(),
                        calculateShare(counterparty.getOwedToMe(), totalOwed),
                        calculateShare(counterparty.getIOwe(), totalIOwe)
                ))
                .toList();
        
        String currencyCode = counterparties.isEmpty()
                ? appSettingsProvider.getOrCreate().getMainCurrency()
                : counterparties.getFirst().getCurrency().getCode();
        
        return new DebtSummaryResponse(currencyCode, normalize(totalOwed), normalize(totalIOwe), summaries);
    }
    
    public Page<DebtTransaction> getHistory(Long userId, DebtTransactionFilter filter, Pageable pageable) {
        Specification<DebtTransaction> specification = Specification
                .where(DebtTransactionSpecifications.belongsToUser(userId))
                .and(DebtTransactionSpecifications.hasCounterparty(filter.counterpartyId()))
                .and(DebtTransactionSpecifications.hasDirection(filter.direction()))
                .and(DebtTransactionSpecifications.occurredAfter(filter.from()))
                .and(DebtTransactionSpecifications.occurredBefore(filter.to()));
        
        return transactionRepository.findAll(specification, pageable);
    }
    
    private BigDecimal calculateShare(BigDecimal value, BigDecimal total) {
        if (value == null || total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        
        return value.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal normalize(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }
}
