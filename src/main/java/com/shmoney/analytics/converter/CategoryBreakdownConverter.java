package com.shmoney.analytics.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shmoney.analytics.model.CategoryBreakdown;
import com.shmoney.common.crypto.EncryptedBigDecimalConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class CategoryBreakdownConverter implements AttributeConverter<List<CategoryBreakdown>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<CategoryBreakdown> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        List<PersistedEntry> entries = attribute.stream()
                .map(entry -> new PersistedEntry(
                        entry.categoryId(),
                        entry.categoryName(),
                        entry.categoryColor(),
                        entry.categoryIcon(),
                        EncryptedBigDecimalConverter.encryptValue(entry.amount()),
                        entry.transactionCount()
                ))
                .collect(Collectors.toList());
        try {
            return OBJECT_MAPPER.writeValueAsString(entries);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize category breakdown", e);
        }
    }

    @Override
    public List<CategoryBreakdown> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<PersistedEntry> entries = OBJECT_MAPPER.readValue(dbData, new TypeReference<>() {
            });
            return entries.stream()
                    .map(entry -> new CategoryBreakdown(
                            entry.categoryId(),
                            entry.categoryName(),
                            entry.categoryColor(),
                            entry.categoryIcon(),
                            decrypt(entry.amountEncrypted()),
                            entry.transactionCount() == null ? 0L : entry.transactionCount()
                    ))
                    .toList();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize category breakdown", e);
        }
    }

    private BigDecimal decrypt(String value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal decrypted = EncryptedBigDecimalConverter.decryptValue(value);
        return decrypted == null ? BigDecimal.ZERO : decrypted;
    }

    private record PersistedEntry(Long categoryId,
                                  String categoryName,
                                  String categoryColor,
                                  String categoryIcon,
                                  String amountEncrypted,
                                  Long transactionCount) {
    }
}
