package com.shmoney.category.service;

import com.shmoney.category.entity.Category;
import com.shmoney.category.entity.Subcategory;
import com.shmoney.category.exception.SubcategoryNotFoundException;
import com.shmoney.category.repository.SubcategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SubcategoryService {

    private final SubcategoryRepository subcategoryRepository;
    private final CategoryService categoryService;

    public SubcategoryService(SubcategoryRepository subcategoryRepository, CategoryService categoryService) {
        this.subcategoryRepository = subcategoryRepository;
        this.categoryService = categoryService;
    }

    public Subcategory create(Long ownerId, Long categoryId, Subcategory subcategory) {
        Category category = categoryService.getOwnedCategory(categoryId, ownerId);
        subcategory.setCategory(category);
        return subcategoryRepository.save(subcategory);
    }

    @Transactional(readOnly = true)
    public List<Subcategory> getByCategory(Long ownerId, Long categoryId) {
        categoryService.getOwnedCategory(categoryId, ownerId);
        return subcategoryRepository.findAllByCategoryIdAndCategoryOwnerIdOrderByNameAsc(categoryId, ownerId);
    }

    @Transactional(readOnly = true)
    public Subcategory getOwnedSubcategory(Long ownerId, Long subcategoryId) {
        return subcategoryRepository.findByIdAndCategoryOwnerId(subcategoryId, ownerId)
                .orElseThrow(() -> new SubcategoryNotFoundException(subcategoryId));
    }

    public Subcategory update(Subcategory subcategory) {
        return subcategoryRepository.save(subcategory);
    }

    public void delete(Subcategory subcategory) {
        subcategoryRepository.delete(subcategory);
    }
}
