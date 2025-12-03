package com.shmoney.budget.controller;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.budget.dto.BudgetCreateRequest;
import com.shmoney.budget.dto.BudgetFilter;
import com.shmoney.budget.dto.BudgetResponse;
import com.shmoney.budget.dto.BudgetUpdateRequest;
import com.shmoney.budget.entity.BudgetPeriodType;
import com.shmoney.budget.entity.BudgetStatus;
import com.shmoney.budget.entity.BudgetType;
import com.shmoney.budget.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Budgets")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final CurrentUserProvider currentUserProvider;

    public BudgetController(BudgetService budgetService, CurrentUserProvider currentUserProvider) {
        this.budgetService = budgetService;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Создать бюджет")
    @PostMapping
    public BudgetResponse create(@Valid @RequestBody BudgetCreateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        return budgetService.create(current.id(), request);
    }

    @Operation(summary = "Список бюджетов")
    @GetMapping
    public List<BudgetResponse> list(@RequestParam(required = false) BudgetStatus status,
                                     @RequestParam(required = false) BudgetPeriodType periodType,
                                     @RequestParam(required = false) BudgetType budgetType,
                                     @RequestParam(required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                     @RequestParam(required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        BudgetFilter filter = new BudgetFilter(status, periodType, budgetType, from, to);
        return budgetService.list(current.id(), filter);
    }

    @Operation(summary = "Получить бюджет по id")
    @GetMapping("/{id}")
    public BudgetResponse getById(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        return budgetService.get(current.id(), id);
    }

    @Operation(summary = "Обновить бюджет")
    @PatchMapping("/{id}")
    public BudgetResponse update(@PathVariable Long id,
                                 @Valid @RequestBody BudgetUpdateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        return budgetService.update(current.id(), id, request);
    }

    @Operation(summary = "Закрыть бюджет")
    @PostMapping("/{id}/close")
    public BudgetResponse close(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        return budgetService.close(current.id(), id);
    }

    @Operation(summary = "Удалить бюджет")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        budgetService.delete(current.id(), id);
    }
}
