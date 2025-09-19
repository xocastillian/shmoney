package com.shmoney.user.controller;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.user.dto.UserCreateRequest;
import com.shmoney.user.dto.UserMapper;
import com.shmoney.user.dto.UserResponse;
import com.shmoney.user.dto.UserUpdateRequest;
import com.shmoney.user.entity.User;
import com.shmoney.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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
    
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        User user = userMapper.toEntity(request);
        User created = userService.create(user);
        UserResponse response = userMapper.toResponse(created);
        return ResponseEntity
                .created(URI.create("/api/users/" + response.id()))
                .body(response);
    }
    
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        ensureCanAccessUser(id);
        return userMapper.toResponse(userService.getById(id));
    }
    
    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }
    
    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        ensureCanAccessUser(id);
        User existing = userService.getById(id);
        userMapper.updateEntity(request, existing);
        User updated = userService.update(existing, existing);
        return userMapper.toResponse(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ensureCanAccessUser(id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    private void ensureCanAccessUser(Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        boolean isOwner = current.id().equals(id);
        if (!isOwner && !currentUserProvider.isAdmin(current)) {
            throw new AccessDeniedException("Forbidden");
        }
    }
}
