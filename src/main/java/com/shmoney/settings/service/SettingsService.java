package com.shmoney.settings.service;

import com.shmoney.settings.ApplicationLanguage;
import com.shmoney.settings.dto.AppSettingsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SettingsService {
    
    private static final List<ApplicationLanguage> SUPPORTED_LANGUAGES =
            List.of(ApplicationLanguage.RU, ApplicationLanguage.EN);
    
    private static final Map<String, ApplicationLanguage> LANGUAGE_BY_CODE = SUPPORTED_LANGUAGES.stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(lang -> lang.code().toLowerCase(Locale.ROOT),
                    lang -> lang));
    
    private static final ApplicationLanguage DEFAULT_LANGUAGE = ApplicationLanguage.RU;
    
    private final AtomicReference<ApplicationLanguage> currentLanguage = new AtomicReference<>(DEFAULT_LANGUAGE);
    
    public AppSettingsResponse getSettings() {
        return new AppSettingsResponse(
                currentLanguage.get().code(),
                SUPPORTED_LANGUAGES.stream().map(ApplicationLanguage::code).toList()
        );
    }
    
    public AppSettingsResponse updateLanguage(String languageCode) {
        ApplicationLanguage language = resolveLanguage(languageCode);
        currentLanguage.set(language);
        return getSettings();
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
