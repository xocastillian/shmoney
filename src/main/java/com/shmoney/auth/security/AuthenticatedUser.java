package com.shmoney.auth.security;

public record AuthenticatedUser(
        Long id,
        String displayName,
        String role
) {
}
