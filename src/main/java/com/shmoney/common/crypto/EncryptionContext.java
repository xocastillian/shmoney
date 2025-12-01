package com.shmoney.common.crypto;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class EncryptionContext {

    private static final AtomicReference<AesGcmCipher> CIPHER = new AtomicReference<>();

    private EncryptionContext() {
    }

    public static void initialize(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("Encryption secret must not be blank");
        }
        CIPHER.set(new AesGcmCipher(EncryptionKeyUtils.deriveKey(secret.trim())));
    }

    public static String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        return getCipher().encrypt(plainText);
    }

    public static String decrypt(String encrypted) {
        if (encrypted == null) {
            return null;
        }
        if (!isEncrypted(encrypted)) {
            return encrypted;
        }
        return getCipher().decrypt(encrypted);
    }

    public static boolean isEncrypted(String value) {
        if (value == null) {
            return false;
        }
        AesGcmCipher cipher = CIPHER.get();
        if (cipher != null) {
            return cipher.isEncrypted(value);
        }
        return value.startsWith(AesGcmCipher.PREFIX);
    }

    private static AesGcmCipher getCipher() {
        AesGcmCipher cipher = CIPHER.get();
        if (cipher == null) {
            cipher = new AesGcmCipher(EncryptionKeyUtils.loadKey());
            if (!CIPHER.compareAndSet(null, cipher)) {
                cipher = Objects.requireNonNull(CIPHER.get());
            }
        }
        return cipher;
    }
}
