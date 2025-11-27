package com.shmoney.settings.dto;

import java.util.List;

public record AppSettingsResponse(
        String defaultLanguage,
        List<String> supportedLanguages
) {
}
