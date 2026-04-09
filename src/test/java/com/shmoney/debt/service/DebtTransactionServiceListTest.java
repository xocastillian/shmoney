package com.shmoney.debt.service;

import com.shmoney.debt.dto.DebtTransactionFilter;
import com.shmoney.debt.entity.DebtTransaction;
import com.shmoney.debt.entity.DebtTransactionKind;
import com.shmoney.debt.repository.DebtCounterpartyRepository;
import com.shmoney.debt.repository.DebtTransactionRepository;
import com.shmoney.wallet.repository.WalletRepository;
import com.shmoney.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtTransactionServiceListTest {

    @Mock
    private DebtTransactionRepository transactionRepository;
    @Mock
    private DebtCounterpartyRepository counterpartyRepository;
    @Mock
    private WalletService walletService;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private com.shmoney.currency.service.ExchangeRateService exchangeRateService;

    private DebtTransactionService service;

    @BeforeEach
    void setUp() {
        service = new DebtTransactionService(
                transactionRepository,
                counterpartyRepository,
                walletService,
                walletRepository,
                exchangeRateService
        );
    }

    @Test
    void getPageShouldRequestOnlyCashFlowTransactions() {
        DebtTransaction transaction = new DebtTransaction();
        transaction.setKind(DebtTransactionKind.CASH_FLOW);

        when(transactionRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(transaction)));

        var result = service.getPage(
                1L,
                new DebtTransactionFilter(null, null, null, null),
                PageRequest.of(0, 50)
        );

        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findAll(any(Specification.class), any(PageRequest.class));
    }
}
