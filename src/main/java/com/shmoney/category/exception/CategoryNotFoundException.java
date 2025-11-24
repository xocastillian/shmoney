package com.shmoney.category.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long id) {
        super("Category not found for id=" + id);
    }
}
