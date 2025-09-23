package com.shmoney.currency.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "exchange_rates")
public class ExchangeRate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "base_currency_id", nullable = false)
    private Currency baseCurrency;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "target_currency_id", nullable = false)
    private Currency targetCurrency;
    
    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal rate;
    
    @Column(name = "fetched_at", nullable = false)
    private OffsetDateTime fetchedAt;
    
    @Column(name = "source", length = 100, nullable = false)
    private String source;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Currency getBaseCurrency() {
        return baseCurrency;
    }
    
    public void setBaseCurrency(Currency baseCurrency) {
        this.baseCurrency = baseCurrency;
    }
    
    public Currency getTargetCurrency() {
        return targetCurrency;
    }
    
    public void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency;
    }
    
    public BigDecimal getRate() {
        return rate;
    }
    
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
    
    public OffsetDateTime getFetchedAt() {
        return fetchedAt;
    }
    
    public void setFetchedAt(OffsetDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
}
