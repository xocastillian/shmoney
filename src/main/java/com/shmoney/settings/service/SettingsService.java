package com.shmoney.settings.service;

import com.shmoney.settings.ApplicationLanguage;
import com.shmoney.settings.dto.AppSettingsResponse;
import com.shmoney.settings.entity.AppSettings;
import com.shmoney.settings.repository.AppSettingsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional
public class SettingsService {
    
    private static final List<ApplicationLanguage> SUPPORTED_LANGUAGES =
            List.of(ApplicationLanguage.RU, ApplicationLanguage.EN);
    
    private static final Map<String, ApplicationLanguage> LANGUAGE_BY_CODE = SUPPORTED_LANGUAGES.stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(lang -> lang.code().toLowerCase(Locale.ROOT),
                    lang -> lang));
    
    private static final ApplicationLanguage DEFAULT_LANGUAGE = ApplicationLanguage.RU;

    private final AppSettingsRepository appSettingsRepository;

    public SettingsService(AppSettingsRepository appSettingsRepository) {
        this.appSettingsRepository = appSettingsRepository;
    }

    public AppSettingsResponse getSettings() {
        AppSettings settings = getOrCreateSettings();
        return buildResponse(settings);
    }
    
    public AppSettingsResponse updateLanguage(String languageCode) {
        ApplicationLanguage language = resolveLanguage(languageCode);
        AppSettings settings = getOrCreateSettings();
        settings.setDefaultLanguage(language.code());
        appSettingsRepository.save(settings);
        return buildResponse(settings);
    }
    
    private AppSettingsResponse buildResponse(AppSettings settings) {
        return new AppSettingsResponse(
                settings.getDefaultLanguage(),
                SUPPORTED_LANGUAGES.stream().map(ApplicationLanguage::code).toList()
        );
    }

    private AppSettings getOrCreateSettings() {
        return appSettingsRepository.findTopByOrderByIdAsc()
                .orElseGet(this::createDefaultSettings);
    }

    private AppSettings createDefaultSettings() {
        AppSettings settings = new AppSettings();
        settings.setDefaultLanguage(DEFAULT_LANGUAGE.code());
        return appSettingsRepository.save(settings);
    }

    private ApplicationLanguage resolveLanguage(String code) {
        if (code == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Language code is required");
        }
        
        ApplicationLanguage language = LANGUAGE_BY_CODE.get(code.toLowerCase(Locale.ROOT));
        
        if (language == null) {
            String supported = Arrays.toString(LANGUAGE_BY_CODE.keySet().toArray());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported language: " + code + ". Supported: " + supported);
        }
        
        return language;
    }
}
