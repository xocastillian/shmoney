package com.shmoney.currency.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collection;

@Component
public class ExchangeRateClient {
    
    private final org.springframework.web.client.RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    
    public ExchangeRateClient(RestTemplateBuilder restTemplateBuilder,
                              @Value("${currency.exchange-rate.base-url}") String baseUrl,
                              @Value("${currency.exchange-rate.api-key}") String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        
        RestTemplateBuilder templateBuilder = restTemplateBuilder;
        
        if (!baseUrl.contains("exchangerate.host")) {
            templateBuilder = templateBuilder.defaultHeader("apikey", apiKey);
        }
        
        this.restTemplate = templateBuilder.build();
    }
    
    public ExchangeRateResponse fetchLatest(String baseCurrency, Collection<String> symbols) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("Exchange rate API key is not configured");
        }
        
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("base", baseCurrency)
                .queryParam("symbols", String.join(",", symbols));
        
        if (baseUrl.contains("exchangerate.host")) {
            uriBuilder.queryParam("access_key", apiKey);
        }
        
        URI uri = uriBuilder
                .build(true)
                .toUri();
        
        ResponseEntity<ExchangeRateResponse> response = restTemplate
                .getForEntity(uri, ExchangeRateResponse.class);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to fetch exchange rates: HTTP " + response.getStatusCode());
        }
        
        ExchangeRateResponse body = response.getBody();
        
        if (body == null) {
            throw new IllegalStateException("Empty response from exchange rate service");
        }
        
        if (!body.success()) {
            String errorDetails = body.error() != null ? body.error().info() : "Unknown error";
            throw new IllegalStateException("Exchange rate provider error: " + errorDetails);
        }
        
        if (body.base() == null || body.rates() == null) {
            throw new IllegalStateException("Incomplete response from exchange rate service");
        }
        
        return body;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExchangeRateResponse(
            boolean success,
            String base,
            java.util.Map<String, java.math.BigDecimal> rates,
            String date,
            ErrorDetails error
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ErrorDetails(String code, String type, String info) {
        }
    }
}
