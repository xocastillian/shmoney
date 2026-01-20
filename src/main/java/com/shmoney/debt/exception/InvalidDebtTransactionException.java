package com.shmoney.debt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDebtTransactionException extends RuntimeException {

    public InvalidDebtTransactionException(String message) {
        super(message);
    }
}
