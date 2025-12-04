package com.shmoney.settings.service;

import com.shmoney.currency.entity.Currency;
import com.shmoney.currency.service.CurrencyService;
import com.shmoney.settings.ApplicationLanguage;
import com.shmoney.settings.dto.AppSettingsResponse;
import com.shmoney.settings.dto.UpdateAppSettingsRequest;
import com.shmoney.settings.entity.AppSettings;
import com.shmoney.settings.repository.AppSettingsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
    private static final String DEFAULT_MAIN_CURRENCY = "KZT";

    private final AppSettingsRepository appSettingsRepository;
    private final CurrencyService currencyService;

    public SettingsService(AppSettingsRepository appSettingsRepository, CurrencyService currencyService) {
        this.appSettingsRepository = appSettingsRepository;
        this.currencyService = currencyService;
    }

    public AppSettingsResponse getSettings() {
        AppSettings settings = getOrCreateSettings();
        return buildResponse(settings);
    }

    public AppSettingsResponse updateSettings(UpdateAppSettingsRequest request) {
        if ((request.language() == null || request.language().isBlank())
                && (request.mainCurrency() == null || request.mainCurrency().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Нужно указать язык или основную валюту");
        }

        AppSettings settings = getOrCreateSettings();

        if (StringUtils.hasText(request.language())) {
            ApplicationLanguage language = resolveLanguage(request.language());
            settings.setDefaultLanguage(language.code());
        }

        if (StringUtils.hasText(request.mainCurrency())) {
            String currency = resolveCurrency(request.mainCurrency());
            settings.setMainCurrency(currency);
        }

        appSettingsRepository.save(settings);
        return buildResponse(settings);
    }

    private AppSettingsResponse buildResponse(AppSettings settings) {
        return new AppSettingsResponse(
                settings.getDefaultLanguage(),
                settings.getMainCurrency(),
                SUPPORTED_LANGUAGES.stream().map(ApplicationLanguage::code).toList(),
                getSupportedCurrencies()
        );
    }

    private AppSettings getOrCreateSettings() {
        return appSettingsRepository.findTopByOrderByIdAsc()
                .orElseGet(this::createDefaultSettings);
    }
    
    private AppSettings createDefaultSettings() {
        AppSettings settings = new AppSettings();
        settings.setDefaultLanguage(DEFAULT_LANGUAGE.code());
        settings.setMainCurrency(DEFAULT_MAIN_CURRENCY);
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

    private String resolveCurrency(String code) {
        if (!StringUtils.hasText(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency code is required");
        }
        Currency currency = currencyService.getActiveByCode(code.trim().toUpperCase(Locale.ROOT));
        return currency.getCode().toUpperCase(Locale.ROOT);
    }

    private List<String> getSupportedCurrencies() {
        return currencyService.getActiveCurrencies().stream()
                .map(Currency::getCode)
                .map(code -> code.toUpperCase(Locale.ROOT))
                .sorted()
                .toList();
    }
}
