package com.shmoney.wallet.repository;

import com.shmoney.wallet.entity.Wallet;
import com.shmoney.wallet.entity.WalletStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findAllByOwnerIdOrderByIdAsc(Long ownerId);

    List<Wallet> findAllByOwnerIdAndStatusOrderByIdAsc(Long ownerId, WalletStatus status);
}
