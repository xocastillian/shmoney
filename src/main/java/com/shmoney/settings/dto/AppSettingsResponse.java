package com.shmoney.settings.dto;

import java.util.List;

public record AppSettingsResponse(
        String defaultLanguage,
        String mainCurrency,
        List<String> supportedLanguages,
        List<String> supportedCurrencies
) {
}
