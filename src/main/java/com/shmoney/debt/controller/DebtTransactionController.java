package com.shmoney.debt.controller;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.common.dto.PageResponse;
import com.shmoney.debt.dto.DebtTransactionCreateRequest;
import com.shmoney.debt.dto.DebtTransactionFilter;
import com.shmoney.debt.dto.DebtTransactionMapper;
import com.shmoney.debt.dto.DebtTransactionResponse;
import com.shmoney.debt.dto.DebtTransactionUpdateRequest;
import com.shmoney.debt.entity.DebtTransaction;
import com.shmoney.debt.entity.DebtTransactionDirection;
import com.shmoney.debt.service.DebtTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@Tag(name = "Debt Transactions")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/debts/transactions")
public class DebtTransactionController {

    private final DebtTransactionService transactionService;
    private final DebtTransactionMapper transactionMapper;
    private final CurrentUserProvider currentUserProvider;

    public DebtTransactionController(DebtTransactionService transactionService,
                                     DebtTransactionMapper transactionMapper,
                                     CurrentUserProvider currentUserProvider) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Создать долговую транзакцию")
    @PostMapping
    public ResponseEntity<DebtTransactionResponse> create(@Valid @RequestBody DebtTransactionCreateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        DebtTransaction created = transactionService.create(current, request);
        DebtTransactionResponse response = transactionMapper.toResponse(created);
        return ResponseEntity.created(URI.create("/api/debts/transactions/" + response.id())).body(response);
    }

    @Operation(summary = "Список долговых транзакций")
    @GetMapping
    public PageResponse<DebtTransactionResponse> list(@RequestParam(defaultValue = "0") int page,
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
        Page<DebtTransaction> resultPage = transactionService.getPage(current.id(), filter, pageable);
        List<DebtTransactionResponse> responses = resultPage.getContent().stream()
                .map(transactionMapper::toResponse)
                .toList();
        return PageResponse.fromPage(resultPage, responses);
    }

    @Operation(summary = "Получить долговую транзакцию")
    @GetMapping("/{id}")
    public DebtTransactionResponse getById(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        return transactionMapper.toResponse(transactionService.getOwnedById(current.id(), id));
    }

    @Operation(summary = "Обновить долговую транзакцию")
    @PatchMapping("/{id}")
    public DebtTransactionResponse update(@PathVariable Long id,
                                          @Valid @RequestBody DebtTransactionUpdateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        DebtTransaction updated = transactionService.update(current, id, request);
        return transactionMapper.toResponse(updated);
    }

    @Operation(summary = "Удалить долговую транзакцию")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        transactionService.delete(current, id);
        return ResponseEntity.noContent().build();
    }
}
