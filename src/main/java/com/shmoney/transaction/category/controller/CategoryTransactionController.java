package com.shmoney.transaction.category.controller;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.common.dto.PageResponse;
import com.shmoney.transaction.category.dto.*;
import com.shmoney.transaction.category.entity.CategoryTransaction;
import com.shmoney.transaction.category.entity.CategoryTransactionType;
import com.shmoney.transaction.category.service.CategoryTransactionService;
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

@Tag(name = "Category Transactions")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/category-transactions")
public class CategoryTransactionController {

    private final CategoryTransactionService transactionService;
    private final CategoryTransactionMapper mapper;
    private final CurrentUserProvider currentUserProvider;

    public CategoryTransactionController(CategoryTransactionService transactionService,
                                         CategoryTransactionMapper mapper,
                                         CurrentUserProvider currentUserProvider) {
        this.transactionService = transactionService;
        this.mapper = mapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Создать транзакцию по категории")
    @PostMapping
    public ResponseEntity<CategoryTransactionResponse> create(@Valid @RequestBody CategoryTransactionCreateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        CategoryTransaction created = transactionService.create(current, request);
        CategoryTransactionResponse response = mapper.toResponse(created);
        return ResponseEntity.created(URI.create("/api/category-transactions/" + response.id())).body(response);
    }

    @Operation(summary = "Список транзакций")
    @GetMapping
    public PageResponse<CategoryTransactionResponse> list(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "50") int size,
                                                          @RequestParam(required = false) Long walletId,
                                                          @RequestParam(required = false) Long categoryId,
                                                          @RequestParam(required = false) CategoryTransactionType type,
                                                          @RequestParam(required = false)
                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                          OffsetDateTime from,
                                                          @RequestParam(required = false)
                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                          OffsetDateTime to) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        CategoryTransactionFilter filter = new CategoryTransactionFilter(walletId, categoryId, type, from, to);
        Pageable pageable = transactionService.buildPageable(page, size);
        Page<CategoryTransaction> resultPage = transactionService.getPage(current.id(), filter, pageable);
        List<CategoryTransactionResponse> responses = resultPage.getContent().stream()
                .map(mapper::toResponse)
                .toList();
        return PageResponse.fromPage(resultPage, responses);
    }

    @Operation(summary = "Получить транзакцию по id")
    @GetMapping("/{id}")
    public CategoryTransactionResponse getById(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        CategoryTransaction transaction = transactionService.getOwnedById(current.id(), id);
        return mapper.toResponse(transaction);
    }

    @Operation(summary = "Обновить транзакцию")
    @PatchMapping("/{id}")
    public CategoryTransactionResponse update(@PathVariable Long id,
                                              @Valid @RequestBody CategoryTransactionUpdateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        CategoryTransaction updated = transactionService.update(current, id, request);
        return mapper.toResponse(updated);
    }

    @Operation(summary = "Удалить транзакцию")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        transactionService.delete(current, id);
        return ResponseEntity.noContent().build();
    }
}
