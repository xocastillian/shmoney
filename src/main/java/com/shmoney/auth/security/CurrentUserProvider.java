package com.shmoney.auth.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserProvider {
    
    private static final String ADMIN_ROLE = "ADMIN";
    
    public Optional<AuthenticatedUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return Optional.of(authenticatedUser);
        }
        
        return Optional.empty();
    }
    
    public AuthenticatedUser requireCurrentUser() {
        return getCurrentUser().orElseThrow(() -> new AccessDeniedException("Authentication required"));
    }
    
    public boolean isAdmin(AuthenticatedUser user) {
        return ADMIN_ROLE.equalsIgnoreCase(user.role());
    }
}
