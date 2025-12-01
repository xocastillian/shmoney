package com.shmoney.common.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

@Converter
public class EncryptedBigDecimalConverter implements AttributeConverter<BigDecimal, String> {
    
    public static String encryptValue(BigDecimal value) {
        if (value == null) {
            return null;
        }
        
        return EncryptionContext.encrypt(value.toPlainString());
    }
    
    public static BigDecimal decryptValue(String value) {
        if (value == null) {
            return null;
        }
        
        if (!EncryptionContext.isEncrypted(value)) {
            return new BigDecimal(value);
        }
        
        String decrypted = EncryptionContext.decrypt(value);
        return new BigDecimal(decrypted);
    }
    
    @Override
    public String convertToDatabaseColumn(BigDecimal attribute) {
        return encryptValue(attribute);
    }
    
    @Override
    public BigDecimal convertToEntityAttribute(String dbData) {
        return decryptValue(dbData);
    }
}
