package com.shmoney.user.exception;

public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(Long id) {
        super("User not found for id=" + id);
    }
    
    public UserNotFoundException(String email) {
        super("User not found for email=" + email);
    }
}
