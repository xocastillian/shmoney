package com.shmoney.debt.controller;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.common.dto.PageResponse;
import com.shmoney.debt.dto.DebtSummaryResponse;
import com.shmoney.debt.dto.DebtTransactionFilter;
import com.shmoney.debt.dto.DebtTransactionMapper;
import com.shmoney.debt.dto.DebtTransactionResponse;
import com.shmoney.debt.entity.DebtTransaction;
import com.shmoney.debt.entity.DebtTransactionDirection;
import com.shmoney.debt.service.DebtAnalyticsService;
import com.shmoney.debt.service.DebtTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@Tag(name = "Debt Analytics")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/debts/analytics")
public class DebtAnalyticsController {
    
    private final DebtAnalyticsService analyticsService;
    private final DebtTransactionService transactionService;
    private final DebtTransactionMapper transactionMapper;
    private final CurrentUserProvider currentUserProvider;
    
    public DebtAnalyticsController(DebtAnalyticsService analyticsService,
                                   DebtTransactionService transactionService,
                                   DebtTransactionMapper transactionMapper,
                                   CurrentUserProvider currentUserProvider) {
        this.analyticsService = analyticsService;
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
        this.currentUserProvider = currentUserProvider;
    }
    
    @Operation(summary = "Сводка по долгам")
    @GetMapping("/summary")
    public DebtSummaryResponse summary() {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        
        return analyticsService.getSummary(current.id());
    }
    
    @Operation(summary = "История долговых операций")
    @GetMapping("/history")
    public PageResponse<DebtTransactionResponse> history(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "50") int size,
                                                         @RequestParam(required = false) Long counterpartyId,
                                                         @RequestParam(required = false) DebtTransactionDirection direction,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                         OffsetDateTime from,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                         OffsetDateTime to) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        DebtTransactionFilter filter = new DebtTransactionFilter(counterpartyId, direction, from, to);
        Pageable pageable = transactionService.buildPageable(page, size);
        Page<DebtTransaction> resultPage = analyticsService.getHistory(current.id(), filter, pageable);
        List<DebtTransactionResponse> responses = resultPage.getContent().stream()
                .map(transactionMapper::toResponse)
                .toList();
        
        return PageResponse.fromPage(resultPage, responses);
    }
}
