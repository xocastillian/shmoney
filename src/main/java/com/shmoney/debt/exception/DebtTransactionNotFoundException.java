package com.shmoney.debt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DebtTransactionNotFoundException extends RuntimeException {

    public DebtTransactionNotFoundException(Long id) {
        super("Debt transaction not found for id=" + id);
    }
}
