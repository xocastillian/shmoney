package com.shmoney.settings.repository;

import com.shmoney.settings.entity.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppSettingsRepository extends JpaRepository<AppSettings, Long> {

    Optional<AppSettings> findTopByOrderByIdAsc();
}
