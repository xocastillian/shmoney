package com.shmoney.wallet.entity;

import com.shmoney.common.crypto.EncryptedBigDecimalConverter;
import com.shmoney.currency.entity.Currency;
import com.shmoney.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wallets")
public class Wallet {

    private static final String DEFAULT_COLOR = "#202020";
    private static final WalletType DEFAULT_TYPE = WalletType.CASH;
    private static final int SCALE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;
    
    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "color", nullable = false, length = 16)
    private String color;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private WalletType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WalletStatus status;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (balance == null) {
            balance = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        } else {
            balance = balance.setScale(SCALE, RoundingMode.HALF_UP);
        }
        if (color == null || color.isBlank()) {
            color = DEFAULT_COLOR;
        }
        if (type == null) {
            type = DEFAULT_TYPE;
        }
        if (status == null) {
            status = WalletStatus.ACTIVE;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
        if (balance != null) {
            balance = balance.setScale(SCALE, RoundingMode.HALF_UP);
        }
        if (color == null || color.isBlank()) {
            color = DEFAULT_COLOR;
        }
        if (type == null) {
            type = DEFAULT_TYPE;
        }
        if (status == null) {
            status = WalletStatus.ACTIVE;
        }
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getOwner() {
        return owner;
    }
    
    public void setOwner(User owner) {
        this.owner = owner;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance == null ? null : balance.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color == null ? null : color.toUpperCase();
    }

    public WalletType getType() {
        return type;
    }

    public void setType(WalletType type) {
        this.type = type;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public void setStatus(WalletStatus status) {
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
