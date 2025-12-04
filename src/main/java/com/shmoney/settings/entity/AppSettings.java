package com.shmoney.settings.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "app_settings")
public class AppSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private static final String DEFAULT_LANGUAGE = "ru";
    private static final String DEFAULT_MAIN_CURRENCY = "KZT";

    @Column(name = "default_language", nullable = false, length = 8)
    private String defaultLanguage;

    @Column(name = "main_currency", nullable = false, length = 10)
    private String mainCurrency;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        var now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (defaultLanguage == null || defaultLanguage.isBlank()) {
            defaultLanguage = DEFAULT_LANGUAGE;
        }
        if (mainCurrency == null || mainCurrency.isBlank()) {
            mainCurrency = DEFAULT_MAIN_CURRENCY;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public String getMainCurrency() {
        return mainCurrency;
    }

    public void setMainCurrency(String mainCurrency) {
        this.mainCurrency = mainCurrency;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
