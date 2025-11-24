package com.shmoney.category.controller;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.category.dto.SubcategoryCreateRequest;
import com.shmoney.category.dto.SubcategoryMapper;
import com.shmoney.category.dto.SubcategoryResponse;
import com.shmoney.category.dto.SubcategoryUpdateRequest;
import com.shmoney.category.entity.Subcategory;
import com.shmoney.category.service.SubcategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Subcategories")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/categories/{categoryId}/subcategories")
public class SubcategoryController {

    private final SubcategoryService subcategoryService;
    private final SubcategoryMapper subcategoryMapper;
    private final CurrentUserProvider currentUserProvider;

    public SubcategoryController(SubcategoryService subcategoryService,
                                 SubcategoryMapper subcategoryMapper,
                                 CurrentUserProvider currentUserProvider) {
        this.subcategoryService = subcategoryService;
        this.subcategoryMapper = subcategoryMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Создать подкатегорию")
    @PostMapping
    public ResponseEntity<SubcategoryResponse> create(@PathVariable Long categoryId,
                                                      @Valid @RequestBody SubcategoryCreateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        Subcategory subcategory = subcategoryMapper.toEntity(request);
        Subcategory created = subcategoryService.create(current.id(), categoryId, subcategory);
        SubcategoryResponse response = subcategoryMapper.toResponse(created);
        return ResponseEntity
                .created(URI.create("/api/categories/" + categoryId + "/subcategories/" + response.id()))
                .body(response);
    }

    @Operation(summary = "Список подкатегорий категории")
    @GetMapping
    public List<SubcategoryResponse> getAll(@PathVariable Long categoryId) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        return subcategoryService.getByCategory(current.id(), categoryId).stream()
                .map(subcategoryMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Обновить подкатегорию")
    @PatchMapping("/{id}")
    public SubcategoryResponse update(@PathVariable Long categoryId,
                                      @PathVariable Long id,
                                      @Valid @RequestBody SubcategoryUpdateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        Subcategory existing = subcategoryService.getOwnedSubcategory(current.id(), id);
        ensureBelongsToCategory(categoryId, existing);
        subcategoryMapper.updateEntity(request, existing);
        Subcategory updated = subcategoryService.update(existing);
        return subcategoryMapper.toResponse(updated);
    }

    @Operation(summary = "Удалить подкатегорию")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long categoryId, @PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        Subcategory existing = subcategoryService.getOwnedSubcategory(current.id(), id);
        ensureBelongsToCategory(categoryId, existing);
        subcategoryService.delete(existing);
        return ResponseEntity.noContent().build();
    }

    private void ensureBelongsToCategory(Long categoryId, Subcategory subcategory) {
        if (!subcategory.getCategory().getId().equals(categoryId)) {
            throw new AccessDeniedException("Forbidden");
        }
    }
}
