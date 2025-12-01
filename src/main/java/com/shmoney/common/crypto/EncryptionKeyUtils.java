package com.shmoney.common.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Properties;

final class EncryptionKeyUtils {
    
    private static final String SYS_PROPERTY = "app.encryption.key";
    private static final String ENV_PROPERTY = "APP_ENCRYPTION_KEY";
    
    private EncryptionKeyUtils() {
    }
    
    static byte[] loadKey() {
        String value = System.getProperty(SYS_PROPERTY);
        
        if (value == null || value.isBlank()) {
            value = System.getenv(ENV_PROPERTY);
        }
        
        if ((value == null || value.isBlank()) && Files.exists(Path.of(".env"))) {
            value = loadFromDotEnv();
        }
        
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Encryption key is not configured. " +
                    "Specify system property '" + SYS_PROPERTY + "' or environment variable '" + ENV_PROPERTY + "'.");
        }
        
        return deriveKey(value.trim());
    }
    
    private static String loadFromDotEnv() {
        Path path = Path.of(".env");
        Properties properties = new Properties();
        
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
            return properties.getProperty(ENV_PROPERTY);
        } catch (IOException e) {
            return null;
        }
    }
    
    static byte[] deriveKey(String secret) {
        Objects.requireNonNull(secret, "secret must not be null");
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
