package com.shmoney.transaction.category.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCategoryTransactionException extends RuntimeException {

    public InvalidCategoryTransactionException(String message) {
        super(message);
    }
}
