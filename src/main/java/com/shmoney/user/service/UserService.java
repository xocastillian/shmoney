package com.shmoney.user.service;

import com.shmoney.user.dto.TelegramUserData;
import com.shmoney.user.entity.User;
import com.shmoney.user.exception.UserNotFoundException;
import com.shmoney.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
    
    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }
    
    public User syncTelegramUser(TelegramUserData data, String defaultRole) {
        if (data == null || data.id() == null) {
            throw new IllegalArgumentException("Telegram user data is incomplete");
        }
        
        return userRepository.findByTelegramUserId(data.id())
                .map(existing -> updateTelegramUser(existing, data, defaultRole))
                .orElseGet(() -> createTelegramUser(data, defaultRole));
    }
    
    public User update(User user) {
        return userRepository.save(user);
    }
    
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        
        userRepository.deleteById(id);
    }
    
    private User createTelegramUser(TelegramUserData data, String defaultRole) {
        User user = new User();
        user.setTelegramUserId(data.id());
        user.setTelegramUsername(resolveTelegramUsername(data));
        user.setTelegramLanguageCode(data.languageCode());
        user.setRole(resolveTelegramRole(defaultRole));
        user.setSubscriptionActive(false);
        
        return userRepository.save(user);
    }
    
    private User updateTelegramUser(User existing, TelegramUserData data, String defaultRole) {
        existing.setTelegramUsername(resolveTelegramUsername(data));
        existing.setTelegramLanguageCode(data.languageCode());
        
        if (!StringUtils.hasText(existing.getRole())) {
            existing.setRole(resolveTelegramRole(defaultRole));
        }
        
        return userRepository.save(existing);
    }
    
    private String resolveTelegramUsername(TelegramUserData data) {
        if (StringUtils.hasText(data.username())) {
            return data.username().trim();
        }
        
        StringBuilder fallback = new StringBuilder();
        
        if (StringUtils.hasText(data.firstName())) {
            fallback.append(data.firstName().trim());
        }
        
        if (StringUtils.hasText(data.lastName())) {
            if (fallback.length() > 0) fallback.append('.');
            
            fallback.append(data.lastName().trim());
        }
        
        if (fallback.length() == 0) fallback.append("user");
        
        fallback.append('.').append(data.id());
        
        return fallback.toString().replaceAll("\\s+", "_");
    }
    
    private String resolveTelegramRole(String configuredRole) {
        String role = StringUtils.hasText(configuredRole) ? configuredRole.trim() : "USER";
        
        return role.toUpperCase();
    }
}
