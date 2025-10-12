package com.shmoney.auth.security;

import com.shmoney.auth.token.TokenPair;
import com.shmoney.config.CookieProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;

@Component
public class TokenCookieService {
    
    private final CookieProperties cookieProperties;
    
    public TokenCookieService(CookieProperties cookieProperties) {
        this.cookieProperties = cookieProperties;
    }
    
    public void writeAuthCookies(HttpServletResponse response, TokenPair tokenPair) {
        ResponseCookie access = buildCookie(
                cookieProperties.accessTokenName(),
                tokenPair.accessToken(),
                secondsUntil(tokenPair.accessTokenExpiresAt())
        );
        
        ResponseCookie refresh = buildCookie(
                cookieProperties.refreshTokenName(),
                tokenPair.refreshToken(),
                secondsUntil(tokenPair.refreshTokenExpiresAt())
        );
        
        addCookieHeader(response, access);
        addCookieHeader(response, refresh);
    }
    
    public void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie access = buildCookie(cookieProperties.accessTokenName(), "", 0);
        ResponseCookie refresh = buildCookie(cookieProperties.refreshTokenName(), "", 0);
        addCookieHeader(response, access);
        addCookieHeader(response, refresh);
    }
    
    public Optional<String> readAccessToken(HttpServletRequest request) {
        return readCookie(request, cookieProperties.accessTokenName());
    }
    
    public Optional<String> readRefreshToken(HttpServletRequest request) {
        return readCookie(request, cookieProperties.refreshTokenName());
    }
    
    private Optional<String> readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .findFirst();
    }
    
    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .path(cookieProperties.path())
                .sameSite(cookieProperties.sameSite());
        if (cookieProperties.domain() != null && !cookieProperties.domain().isBlank()) {
            builder.domain(cookieProperties.domain());
        }
        
        if (maxAgeSeconds >= 0) {
            builder.maxAge(Duration.ofSeconds(maxAgeSeconds));
        }
        
        return builder.build();
    }
    
    private void addCookieHeader(HttpServletResponse response, ResponseCookie cookie) {
        String headerValue = cookie.toString();
        
        if (cookieProperties.partitioned()) {
            headerValue = headerValue + "; Partitioned";
        }
        
        response.addHeader(HttpHeaders.SET_COOKIE, headerValue);
    }
    
    private long secondsUntil(OffsetDateTime expiresAt) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        long seconds = expiresAt.toEpochSecond() - now.toEpochSecond();
        return Math.max(0, seconds);
    }
}
