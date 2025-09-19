package com.shmoney.auth.token;

public enum TokenType {
    ACCESS("ACCESS"),
    REFRESH("REFRESH");

    private final String claimValue;

    TokenType(String claimValue) {
        this.claimValue = claimValue;
    }

    public String claimValue() {
        return claimValue;
    }
}
