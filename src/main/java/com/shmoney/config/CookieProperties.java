package com.shmoney.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.cookies")
public record CookieProperties(
        String accessTokenName,
        String refreshTokenName,
        String domain,
        String path,
        String sameSite,
        boolean secure,
        boolean partitioned
) {
    public CookieProperties {
        if (accessTokenName == null || accessTokenName.isBlank()) accessTokenName = "access_token";
        if (refreshTokenName == null || refreshTokenName.isBlank()) refreshTokenName = "refresh_token";
        if (path == null || path.isBlank()) path = "/";
        if (sameSite == null || sameSite.isBlank()) sameSite = "Lax";
    }
}
