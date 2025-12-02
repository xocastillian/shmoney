package com.shmoney.budget.exception;

public class BudgetNotFoundException extends RuntimeException {

    public BudgetNotFoundException(Long id) {
        super("Бюджет не найден: " + id);
    }
}
