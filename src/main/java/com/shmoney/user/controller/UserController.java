package com.shmoney.user.controller;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.user.dto.UserMapper;
import com.shmoney.user.dto.UserResponse;
import com.shmoney.user.dto.UserUpdateRequest;
import com.shmoney.user.entity.User;
import com.shmoney.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    private final UserMapper userMapper;
    private final CurrentUserProvider currentUserProvider;
    
    public UserController(UserService userService, UserMapper userMapper, CurrentUserProvider currentUserProvider) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.currentUserProvider = currentUserProvider;
    }
    
    @Operation(summary = "Получить пользователя по id")
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        ensureCanAccessUser(id);
        
        return userMapper.toResponse(userService.getById(id));
    }
    
    @Operation(summary = "Получить текущего пользователя")
    @GetMapping
    public UserResponse getCurrent() {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        User user = userService.getById(current.id());
        return userMapper.toResponse(user);
    }
    
    @Operation(summary = "Обновить пользователя по id")
    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        ensureCanAccessUser(id, current);
        User existing = userService.getById(id);
        boolean originalSubscription = existing.isSubscriptionActive();
        userMapper.updateEntity(request, existing);
        existing.setSubscriptionActive(originalSubscription);

        User updated = userService.update(existing);

        return userMapper.toResponse(updated);
    }
    
    @Operation(summary = "Удалить пользователя по id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ensureCanAccessUser(id);
        userService.delete(id);
        
        return ResponseEntity.noContent().build();
    }
    
    private void ensureCanAccessUser(Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        ensureCanAccessUser(id, current);
    }

    private void ensureCanAccessUser(Long id, AuthenticatedUser current) {
        boolean isOwner = current.id().equals(id);
        
        if (!isOwner) {
            throw new AccessDeniedException("Forbidden");
        }
    }
}
