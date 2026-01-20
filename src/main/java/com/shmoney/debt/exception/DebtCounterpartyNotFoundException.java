package com.shmoney.debt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DebtCounterpartyNotFoundException extends RuntimeException {

    public DebtCounterpartyNotFoundException(Long id) {
        super("Debt counterparty not found for id=" + id);
    }
}
