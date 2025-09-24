package com.shmoney.user.repository;

import com.shmoney.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"wallets", "wallets.currency"})
    Optional<User> findById(Long id);

    @EntityGraph(attributePaths = {"wallets", "wallets.currency"})
    List<User> findAll();

    Optional<User> findByTelegramUserId(Long telegramUserId);
}
