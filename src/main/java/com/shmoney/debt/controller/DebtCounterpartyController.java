package com.shmoney.debt.controller;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.debt.dto.DebtCounterpartyCreateRequest;
import com.shmoney.debt.dto.DebtCounterpartyMapper;
import com.shmoney.debt.dto.DebtCounterpartyResponse;
import com.shmoney.debt.dto.DebtCounterpartyUpdateRequest;
import com.shmoney.debt.entity.DebtCounterparty;
import com.shmoney.debt.entity.DebtCounterpartyStatus;
import com.shmoney.debt.service.DebtCounterpartyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Debt Counterparties")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/debts/counterparties")
public class DebtCounterpartyController {
    
    private final DebtCounterpartyService counterpartyService;
    private final DebtCounterpartyMapper counterpartyMapper;
    private final CurrentUserProvider currentUserProvider;
    
    public DebtCounterpartyController(DebtCounterpartyService counterpartyService,
                                      DebtCounterpartyMapper counterpartyMapper,
                                      CurrentUserProvider currentUserProvider) {
        this.counterpartyService = counterpartyService;
        this.counterpartyMapper = counterpartyMapper;
        this.currentUserProvider = currentUserProvider;
    }
    
    @Operation(summary = "Создать контрагента")
    @PostMapping
    public ResponseEntity<DebtCounterpartyResponse> create(@Valid @RequestBody DebtCounterpartyCreateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        DebtCounterparty created = counterpartyService.create(current.id(), request);
        DebtCounterpartyResponse response = counterpartyMapper.toResponse(created);
        
        return ResponseEntity.created(URI.create("/api/debts/counterparties/" + response.id())).body(response);
    }
    
    @Operation(summary = "Список контрагентов")
    @GetMapping
    public List<DebtCounterpartyResponse> list(@RequestParam(required = false) DebtCounterpartyStatus status) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        
        return counterpartyService.list(current.id(), status).stream()
                .map(counterpartyMapper::toResponse)
                .toList();
    }
    
    @Operation(summary = "Получить контрагента по id")
    @GetMapping("/{id}")
    public DebtCounterpartyResponse getById(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        
        return counterpartyMapper.toResponse(counterpartyService.getOwned(current.id(), id));
    }
    
    @Operation(summary = "Обновить контрагента")
    @PatchMapping("/{id}")
    public DebtCounterpartyResponse update(@PathVariable Long id,
                                           @Valid @RequestBody DebtCounterpartyUpdateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        
        return counterpartyMapper.toResponse(counterpartyService.update(current.id(), id, request));
    }
    
    @Operation(summary = "Архивировать контрагента")
    @PostMapping("/{id}/archive")
    public DebtCounterpartyResponse archive(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        
        return counterpartyMapper.toResponse(counterpartyService.archive(current.id(), id));
    }
    
    @Operation(summary = "Восстановить контрагента")
    @PostMapping("/{id}/restore")
    public DebtCounterpartyResponse restore(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        
        return counterpartyMapper.toResponse(counterpartyService.restore(current.id(), id));
    }
}
