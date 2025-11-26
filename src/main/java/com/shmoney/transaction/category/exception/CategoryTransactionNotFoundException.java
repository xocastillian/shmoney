package com.shmoney.transaction.category.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CategoryTransactionNotFoundException extends RuntimeException {

    public CategoryTransactionNotFoundException(Long id) {
        super("Category transaction not found for id=" + id);
    }
}
