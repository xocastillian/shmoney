package com.shmoney.category.exception;

public class SubcategoryNotFoundException extends RuntimeException {

    public SubcategoryNotFoundException(Long id) {
        super("Subcategory not found for id=" + id);
    }
}
