package com.shmoney.analytics.repository;

import com.shmoney.analytics.entity.MonthlyAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface MonthlyAnalyticsRepository extends JpaRepository<MonthlyAnalytics, Long> {

    Optional<MonthlyAnalytics> findByUserIdAndPeriodStart(Long userId, OffsetDateTime periodStart);

    List<MonthlyAnalytics> findAllByUserId(Long userId);

    void deleteByUserIdAndPeriodStart(Long userId, OffsetDateTime periodStart);
}
