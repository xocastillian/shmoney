package com.shmoney.debt.entity;

import com.shmoney.common.crypto.EncryptedBigDecimalConverter;
import com.shmoney.currency.entity.Currency;
import com.shmoney.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Entity
@Table(name = "debt_counterparties")
public class DebtCounterparty {

    private static final int AMOUNT_SCALE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 16)
    private String color;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "owed_to_me", nullable = false)
    private BigDecimal owedToMe;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "i_owe", nullable = false)
    private BigDecimal iOwe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DebtCounterpartyStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        normalizeAmounts();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
        normalizeAmounts();
    }

    @PostLoad
    void onLoad() {
        normalizeAmounts();
    }

    private void normalizeAmounts() {
        if (owedToMe == null) {
            owedToMe = BigDecimal.ZERO.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        } else {
            owedToMe = owedToMe.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        }
        if (iOwe == null) {
            iOwe = BigDecimal.ZERO.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        } else {
            iOwe = iOwe.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getOwedToMe() {
        return owedToMe;
    }

    public void setOwedToMe(BigDecimal owedToMe) {
        this.owedToMe = owedToMe;
    }

    public BigDecimal getIOwe() {
        return iOwe;
    }

    public void setIOwe(BigDecimal iOwe) {
        this.iOwe = iOwe;
    }

    public DebtCounterpartyStatus getStatus() {
        return status;
    }

    public void setStatus(DebtCounterpartyStatus status) {
        this.status = status;
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
