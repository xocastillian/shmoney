package com.shmoney.wallet.transaction.repository;

import com.shmoney.wallet.transaction.entity.WalletTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    @EntityGraph(attributePaths = {"fromWallet", "fromWallet.owner", "toWallet", "toWallet.owner", "sourceCurrency", "targetCurrency"})
    List<WalletTransaction> findAllByFromWalletIdOrToWalletIdOrderByExecutedAtDesc(Long fromWalletId, Long toWalletId);

    @EntityGraph(attributePaths = {"fromWallet", "fromWallet.owner", "toWallet", "toWallet.owner", "sourceCurrency", "targetCurrency"})
    List<WalletTransaction> findAllByOrderByExecutedAtDesc();

    @Override
    @EntityGraph(attributePaths = {"fromWallet", "fromWallet.owner", "toWallet", "toWallet.owner", "sourceCurrency", "targetCurrency"})
    Optional<WalletTransaction> findById(Long id);
}
