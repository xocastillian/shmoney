package com.shmoney.common.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.encryption")
public record AppEncryptionProperties(String key) {
}
