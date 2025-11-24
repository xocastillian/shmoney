package com.shmoney.category.service;

import com.shmoney.category.entity.Category;
import com.shmoney.category.entity.Subcategory;
import com.shmoney.category.exception.CategoryNotFoundException;
import com.shmoney.category.repository.CategoryRepository;
import com.shmoney.user.entity.User;
import com.shmoney.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public CategoryService(CategoryRepository categoryRepository, UserService userService) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    public Category create(Long ownerId, Category category) {
        User owner = userService.getById(ownerId);
        category.setOwner(owner);
        attachSubcategories(category);
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> getAll(Long ownerId) {
        return categoryRepository.findAllByOwnerIdOrderByNameAsc(ownerId);
    }

    @Transactional(readOnly = true)
    public Category getOwnedCategory(Long id, Long ownerId) {
        return categoryRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    public Category update(Category category) {
        attachSubcategories(category);
        return categoryRepository.save(category);
    }

    public void delete(Category category) {
        categoryRepository.delete(category);
    }

    private void attachSubcategories(Category category) {
        if (category.getSubcategories() == null) {
            category.setSubcategories(new java.util.ArrayList<>());
            return;
        }
        for (Subcategory subcategory : category.getSubcategories()) {
            subcategory.setCategory(category);
        }
    }
}
