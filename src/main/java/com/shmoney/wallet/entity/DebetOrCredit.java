package com.shmoney.wallet.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DebetOrCredit {
    DEBET("debet"),
    CREDIT("credit");
    
    private final String value;
    
    DebetOrCredit(String value) {
        this.value = value;
    }
    
    @JsonCreator
    public static DebetOrCredit fromValue(String raw) {
        if (raw == null) {
            return null;
        }
        
        String normalized = raw.trim().toUpperCase();
        
        for (DebetOrCredit option : values()) {
            if (option.name().equals(normalized) || option.value.equalsIgnoreCase(raw)) {
                return option;
            }
        }
        
        throw new IllegalArgumentException("Unknown debetOrCredit value: " + raw);
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
}
