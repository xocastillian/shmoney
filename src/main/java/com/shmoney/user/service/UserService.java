package com.shmoney.user.service;

import com.shmoney.user.dto.TelegramUserData;
import com.shmoney.user.entity.User;
import com.shmoney.user.exception.UserNotFoundException;
import com.shmoney.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    
    public User syncTelegramUser(TelegramUserData data) {
        if (data == null || data.id() == null) {
            throw new IllegalArgumentException("Telegram user data is incomplete");
        }
        
        return userRepository.findByTelegramUserId(data.id())
                .map(existing -> updateTelegramUser(existing, data))
                .orElseGet(() -> createTelegramUser(data));
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
    
    private User createTelegramUser(TelegramUserData data) {
        User user = new User();
        user.setTelegramUserId(data.id());
        user.setTelegramUsername(resolveTelegramUsername(data));
        user.setTelegramLanguageCode(data.languageCode());
        user.setSubscriptionActive(false);
        
        return userRepository.save(user);
    }
    
    private User updateTelegramUser(User existing, TelegramUserData data) {
        existing.setTelegramUsername(resolveTelegramUsername(data));
        existing.setTelegramLanguageCode(data.languageCode());
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
    
}
