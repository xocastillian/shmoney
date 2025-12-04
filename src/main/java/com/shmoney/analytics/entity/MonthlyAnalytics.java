package com.shmoney.analytics.entity;

import com.shmoney.analytics.converter.CategoryBreakdownConverter;
import com.shmoney.analytics.model.CategoryBreakdown;
import com.shmoney.common.crypto.EncryptedBigDecimalConverter;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analytics_monthly_summary",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "period_start"}))
public class MonthlyAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "period_start", nullable = false)
    private OffsetDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private OffsetDateTime periodEnd;

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "total_expense", nullable = false)
    private BigDecimal totalExpense;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "total_income", nullable = false)
    private BigDecimal totalIncome;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "cash_flow_amount", nullable = false)
    private BigDecimal cashFlowAmount;

    @Column(name = "cash_flow_percent", nullable = false, precision = 8, scale = 2)
    private BigDecimal cashFlowPercent;

    @Convert(converter = CategoryBreakdownConverter.class)
    @Column(name = "expense_breakdown", columnDefinition = "TEXT")
    private List<CategoryBreakdown> expenseBreakdown = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getCashFlowAmount() {
        return cashFlowAmount;
    }

    public void setCashFlowAmount(BigDecimal cashFlowAmount) {
        this.cashFlowAmount = cashFlowAmount;
    }

    public BigDecimal getCashFlowPercent() {
        return cashFlowPercent;
    }

    public void setCashFlowPercent(BigDecimal cashFlowPercent) {
        this.cashFlowPercent = cashFlowPercent;
    }

    public List<CategoryBreakdown> getExpenseBreakdown() {
        return expenseBreakdown;
    }

    public void setExpenseBreakdown(List<CategoryBreakdown> expenseBreakdown) {
        this.expenseBreakdown = expenseBreakdown;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
