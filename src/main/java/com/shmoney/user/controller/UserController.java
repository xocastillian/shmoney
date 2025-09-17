package com.shmoney.user.controller;

import com.shmoney.user.dto.UserCreateRequest;
import com.shmoney.user.dto.UserMapper;
import com.shmoney.user.dto.UserResponse;
import com.shmoney.user.dto.UserUpdateRequest;
import com.shmoney.user.entity.User;
import com.shmoney.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    private final UserMapper userMapper;
    
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }
    
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        // DTO -> Entity
        User user = userMapper.toEntity(request);
        User created = userService.create(user);
        // Entity -> DTO
        UserResponse response = userMapper.toResponse(created);
        return ResponseEntity
                .created(URI.create("/api/users/" + response.id()))
                .body(response);
    }
    
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userMapper.toResponse(userService.getById(id));
    }
    
    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }
    
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        User existing = userService.getById(id);
        // DTO -> Entity
        userMapper.updateEntity(request, existing);
        User updated = userService.update(existing, existing);
        return userMapper.toResponse(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
