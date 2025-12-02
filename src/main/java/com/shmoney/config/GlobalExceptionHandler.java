package com.shmoney.config;

import com.shmoney.auth.exception.TelegramAuthenticationException;
import com.shmoney.budget.exception.BudgetNotFoundException;
import com.shmoney.budget.exception.InvalidBudgetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("403 AccessDenied: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "forbidden", "message", ex.getMessage()));
    }
    
    @ExceptionHandler(TelegramAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleTelegramAuth(TelegramAuthenticationException ex) {
        log.warn("401 TelegramAuth: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "unauthorized", "message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidBudgetException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBudget(InvalidBudgetException ex) {
        log.warn("400 InvalidBudget: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "bad_request", "message", ex.getMessage()));
    }

    @ExceptionHandler(BudgetNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBudgetNotFound(BudgetNotFoundException ex) {
        log.warn("404 BudgetNotFound: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "not_found", "message", ex.getMessage()));
    }
}
