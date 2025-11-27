package com.shmoney.settings;

public enum ApplicationLanguage {
    RU("ru"),
    EN("en");

    private final String code;

    ApplicationLanguage(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
