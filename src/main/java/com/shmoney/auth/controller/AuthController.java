package com.shmoney.auth.controller;

import com.shmoney.auth.dto.AuthResponse;
import com.shmoney.auth.dto.RefreshRequest;
import com.shmoney.auth.dto.TelegramAuthRequest;
import com.shmoney.auth.service.AuthService;
import com.shmoney.auth.service.TelegramAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    private final TelegramAuthService telegramAuthService;
    
    public AuthController(AuthService authService, TelegramAuthService telegramAuthService) {
        this.authService = authService;
        this.telegramAuthService = telegramAuthService;
    }
    
    @Operation(summary = "Обновить токены")
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }
    
    @Operation(summary = "Авторизация через Telegram Web App")
    @PostMapping("/telegram")
    public AuthResponse telegramLogin(@Valid @RequestBody TelegramAuthRequest request) {
        return telegramAuthService.authenticate(request);
    }
}
