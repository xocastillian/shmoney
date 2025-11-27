package com.shmoney.settings.controller;

import com.shmoney.settings.dto.AppSettingsResponse;
import com.shmoney.settings.dto.UpdateAppSettingsRequest;
import com.shmoney.settings.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Settings")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Operation(summary = "Получить настройки приложения")
    @GetMapping
    public AppSettingsResponse getSettings() {
        return settingsService.getSettings();
    }

    @Operation(summary = "Обновить настройки приложения")
    @PatchMapping
    public AppSettingsResponse updateSettings(@Valid @RequestBody UpdateAppSettingsRequest request) {
        return settingsService.updateLanguage(request.language());
    }
}
