package com.shmoney.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "telegram")
public record TelegramProperties(
        boolean enabled,
        String botToken,
        Duration initDataMaxAge
) {
    public TelegramProperties {
        if (initDataMaxAge == null) {
            initDataMaxAge = Duration.ofMinutes(5);
        }
    }
}
