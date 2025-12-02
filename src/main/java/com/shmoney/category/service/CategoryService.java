package com.shmoney.category.service;

import com.shmoney.category.entity.Category;
import com.shmoney.category.entity.CategoryStatus;
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
        category.setStatus(CategoryStatus.ACTIVE);
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
        return categoryRepository.save(category);
    }

    public Category updateStatus(Category category, CategoryStatus status) {
        if (status == null) {
            return category;
        }
        category.setStatus(status);
        return categoryRepository.save(category);
    }
}
