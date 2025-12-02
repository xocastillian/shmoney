package com.shmoney.budget.entity;

import com.shmoney.category.entity.Category;
import com.shmoney.common.crypto.EncryptedBigDecimalConverter;
import com.shmoney.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "budgets")
public class Budget {

    private static final int AMOUNT_SCALE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private BudgetPeriodType periodType;

    @Column(name = "period_start", nullable = false)
    private OffsetDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private OffsetDateTime periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_type", nullable = false, length = 20)
    private BudgetType budgetType;

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "amount_limit", nullable = false)
    private BigDecimal amountLimit;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "spent_amount", nullable = false)
    private BigDecimal spentAmount;

    @Column(name = "percent_spent", nullable = false, precision = 8, scale = 2)
    private BigDecimal percentSpent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BudgetStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @ManyToMany
    @JoinTable(name = "budget_categories",
            joinColumns = @JoinColumn(name = "budget_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

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

    private void normalizeAmounts() {
        if (amountLimit != null) {
            amountLimit = amountLimit.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        }
        if (spentAmount != null) {
            spentAmount = spentAmount.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        }
        if (percentSpent != null) {
            percentSpent = percentSpent.setScale(2, RoundingMode.HALF_UP);
        }
    }

    public Long getId() {
        return id;
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

    public BudgetPeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(BudgetPeriodType periodType) {
        this.periodType = periodType;
    }

    public OffsetDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(OffsetDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public OffsetDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(OffsetDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public BudgetType getBudgetType() {
        return budgetType;
    }

    public void setBudgetType(BudgetType budgetType) {
        this.budgetType = budgetType;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public BigDecimal getPercentSpent() {
        return percentSpent;
    }

    public void setPercentSpent(BigDecimal percentSpent) {
        this.percentSpent = percentSpent;
    }

    public BudgetStatus getStatus() {
        return status;
    }

    public void setStatus(BudgetStatus status) {
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

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(OffsetDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }
}
