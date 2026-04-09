package com.shmoney.debt.service;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.currency.entity.Currency;
import com.shmoney.debt.dto.DebtForgiveRequest;
import com.shmoney.debt.entity.DebtCounterparty;
import com.shmoney.debt.entity.DebtTransaction;
import com.shmoney.debt.entity.DebtTransactionDirection;
import com.shmoney.debt.entity.DebtTransactionKind;
import com.shmoney.debt.exception.InvalidDebtTransactionException;
import com.shmoney.debt.repository.DebtCounterpartyRepository;
import com.shmoney.debt.repository.DebtTransactionRepository;
import com.shmoney.wallet.repository.WalletRepository;
import com.shmoney.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtTransactionServiceForgiveTest {

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
    void forgiveShouldCreateNonCashFlowTransactionAndKeepWalletUntouched() {
        AuthenticatedUser currentUser = new AuthenticatedUser(1L, "telegram");
        DebtCounterparty counterparty = new DebtCounterparty();
        counterparty.setOwedToMe(new BigDecimal("20000.00"));
        counterparty.setIOwe(BigDecimal.ZERO);
        counterparty.setCurrency(currency("RUB"));
        counterparty.setUser(new com.shmoney.user.entity.User());

        when(counterpartyRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(counterparty));
        when(transactionRepository.save(any(DebtTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(counterpartyRepository.save(any(DebtCounterparty.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-04-09T12:00:00+02:00");
        service.forgive(currentUser, 7L, new DebtForgiveRequest(occurredAt, "Forgiven"));

        ArgumentCaptor<DebtTransaction> transactionCaptor = ArgumentCaptor.forClass(DebtTransaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        DebtTransaction saved = transactionCaptor.getValue();
        assertThat(saved.getKind()).isEqualTo(DebtTransactionKind.FORGIVEN);
        assertThat(saved.getDirection()).isEqualTo(DebtTransactionDirection.BORROWED);
        assertThat(saved.getAmount()).isEqualByComparingTo("20000.00");
        assertThat(saved.getWallet()).isNull();

        assertThat(counterparty.getOwedToMe()).isEqualByComparingTo("0.00");
        assertThat(counterparty.getIOwe()).isEqualByComparingTo("0.00");
        verify(walletRepository, never()).save(any());
    }

    @Test
    void forgiveShouldFailWhenNoDebtExists() {
        AuthenticatedUser currentUser = new AuthenticatedUser(1L, "telegram");
        DebtCounterparty counterparty = new DebtCounterparty();
        counterparty.setOwedToMe(BigDecimal.ZERO);
        counterparty.setIOwe(BigDecimal.ZERO);
        counterparty.setCurrency(currency("RUB"));

        when(counterpartyRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(counterparty));

        assertThatThrownBy(() -> service.forgive(
                currentUser,
                7L,
                new DebtForgiveRequest(OffsetDateTime.parse("2026-04-09T12:00:00+02:00"), "Forgiven")
        )).isInstanceOf(InvalidDebtTransactionException.class)
                .hasMessageContaining("нет долга");
    }

    private Currency currency(String code) {
        Currency currency = new Currency();
        currency.setCode(code);
        return currency;
    }
}
