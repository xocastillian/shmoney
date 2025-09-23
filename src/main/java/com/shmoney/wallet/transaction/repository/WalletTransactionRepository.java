package com.shmoney.wallet.transaction.repository;

import com.shmoney.wallet.transaction.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findAllByFromWalletIdOrToWalletIdOrderByExecutedAtDesc(Long fromWalletId, Long toWalletId);

    List<WalletTransaction> findAllByOrderByExecutedAtDesc();
}
