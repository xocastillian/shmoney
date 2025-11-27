package com.shmoney.auth.controller;

import com.shmoney.auth.dto.AuthResponse;
import com.shmoney.auth.dto.RefreshRequest;
import com.shmoney.auth.dto.TelegramAuthRequest;
import com.shmoney.auth.security.TokenCookieService;
import com.shmoney.auth.service.AuthService;
import com.shmoney.auth.service.TelegramAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final TokenCookieService tokenCookieService;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    public AuthController(AuthService authService, TelegramAuthService telegramAuthService,
                          TokenCookieService tokenCookieService) {
        this.authService = authService;
        this.telegramAuthService = telegramAuthService;
        this.tokenCookieService = tokenCookieService;
    }
    
    @Operation(summary = "Обновить токены")
    @PostMapping("/refresh")
    public AuthResponse refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                @Valid @RequestBody(required = false) RefreshRequest request) {
        String refreshToken = request != null && request.refreshToken() != null
                ? request.refreshToken()
                : tokenCookieService.readRefreshToken(httpRequest).orElse(null);
        
        logger.info("POST /api/auth/refresh (cookiePresent={}, bodyPresent={})",
                tokenCookieService.readRefreshToken(httpRequest).isPresent(),
                request != null && request.refreshToken() != null);
        
        AuthResponse result = authService.refresh(refreshToken);
        tokenCookieService.writeAuthCookies(httpResponse, new com.shmoney.auth.token.TokenPair(
                result.accessToken(), result.accessTokenExpiresAt(),
                result.refreshToken(), result.refreshTokenExpiresAt()
        ));
        
        return result;
    }
    
    @Operation(summary = "Авторизация через Telegram Web App")
    @PostMapping("/telegram")
    public AuthResponse telegramLogin(HttpServletResponse httpResponse,
                                      @Valid @RequestBody TelegramAuthRequest request) {
        String initData = request.initData();
        int len = initData != null ? initData.length() : 0;
        String snippet = initData != null ? initData.substring(0, Math.min(120, initData.length())) : "";
        
        logger.info("POST /api/auth/telegram len={} snippet={}...", len, snippet);
        
        AuthResponse result = telegramAuthService.authenticate(request);
        
        tokenCookieService.writeAuthCookies(httpResponse, new com.shmoney.auth.token.TokenPair(
                result.accessToken(), result.accessTokenExpiresAt(),
                result.refreshToken(), result.refreshTokenExpiresAt()
        ));
        
        return result;
    }
}
