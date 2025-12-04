package com.shmoney.settings.service;

import com.shmoney.settings.entity.AppSettings;
import com.shmoney.settings.repository.AppSettingsRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AppSettingsProvider {

    private final AppSettingsRepository appSettingsRepository;

    public AppSettingsProvider(AppSettingsRepository appSettingsRepository) {
        this.appSettingsRepository = appSettingsRepository;
    }

    public AppSettings getOrCreate() {
        return appSettingsRepository.findTopByOrderByIdAsc()
                .orElseGet(() -> appSettingsRepository.save(new AppSettings()));
    }
}
