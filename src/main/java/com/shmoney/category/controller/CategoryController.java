package com.shmoney.category.controller;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.category.dto.CategoryCreateRequest;
import com.shmoney.category.dto.CategoryMapper;
import com.shmoney.category.dto.CategoryResponse;
import com.shmoney.category.dto.CategoryUpdateRequest;
import com.shmoney.category.entity.Category;
import com.shmoney.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Categories")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final CurrentUserProvider currentUserProvider;

    public CategoryController(CategoryService categoryService,
                              CategoryMapper categoryMapper,
                              CurrentUserProvider currentUserProvider) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Создать категорию")
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        Category category = categoryMapper.toEntity(request);
        Category created = categoryService.create(current.id(), category);
        CategoryResponse response = categoryMapper.toResponse(created);

        return ResponseEntity
                .created(URI.create("/api/categories/" + response.id()))
                .body(response);
    }

    @Operation(summary = "Список категорий текущего пользователя")
    @GetMapping
    public List<CategoryResponse> getAll() {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        return categoryService.getAll(current.id()).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Получить категорию по id")
    @GetMapping("/{id}")
    public CategoryResponse getById(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        Category category = categoryService.getOwnedCategory(id, current.id());
        return categoryMapper.toResponse(category);
    }

    @Operation(summary = "Обновить категорию")
    @PatchMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id,
                                   @Valid @RequestBody CategoryUpdateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        Category existing = categoryService.getOwnedCategory(id, current.id());
        categoryMapper.updateEntity(request, existing);
        Category updated = categoryService.update(existing);
        return categoryMapper.toResponse(updated);
    }

    @Operation(summary = "Удалить категорию")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        Category existing = categoryService.getOwnedCategory(id, current.id());
        categoryService.delete(existing);
        return ResponseEntity.noContent().build();
    }
}
