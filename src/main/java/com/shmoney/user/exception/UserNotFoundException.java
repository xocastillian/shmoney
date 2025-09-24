package com.shmoney.user.exception;

public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(Long id) {
        super("User not found for id=" + id);
    }
}
