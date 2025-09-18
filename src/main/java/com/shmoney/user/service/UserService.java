package com.shmoney.user.service;

import com.shmoney.user.entity.User;
import com.shmoney.user.exception.UserNotFoundException;
import com.shmoney.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public User create(User user) {
        user.setId(null);
        user.setPasswordHash(encodePassword(user.getPasswordHash()));
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
    
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
    
    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }
    
    public User update(User existing, User changes) {
        existing.setName(changes.getName());
        existing.setEmail(changes.getEmail());
        existing.setRole(changes.getRole());
        existing.setSubscriptionActive(changes.isSubscriptionActive());
        existing.setLastLoginAt(changes.getLastLoginAt());
        
        String newPassword = changes.getPasswordHash();
        if (newPassword != null && !newPassword.isBlank()) {
            existing.setPasswordHash(passwordEncoder.encode(newPassword));
        }
        
        return userRepository.save(existing);
    }
    
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
    
    private String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        return passwordEncoder.encode(rawPassword);
    }
}
