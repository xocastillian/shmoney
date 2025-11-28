package com.shmoney.auth.security;

import com.shmoney.auth.exception.InvalidTokenException;
import com.shmoney.auth.service.JwtTokenService;
import com.shmoney.user.entity.User;
import com.shmoney.user.exception.UserNotFoundException;
import com.shmoney.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final JwtTokenService jwtTokenService;
    private final UserService userService;
    private final TokenCookieService tokenCookieService;
    
    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, UserService userService,
                                   TokenCookieService tokenCookieService) {
        this.jwtTokenService = jwtTokenService;
        this.userService = userService;
        this.tokenCookieService = tokenCookieService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            token = header.substring(BEARER_PREFIX.length());
        } else {
            token = tokenCookieService.readAccessToken(request).orElse(null);
        }
        
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                var details = jwtTokenService.parseAccessToken(token);
                User user = userService.getById(details.userId());
                setAuthentication(user, token);
            } catch (InvalidTokenException | UserNotFoundException ex) {
                SecurityContextHolder.clearContext();
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void setAuthentication(User user, String token) {
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getTelegramUsername());
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken(principal, token, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        
        if (uri.startsWith("/v3/") || uri.startsWith("/swagger-ui") || uri.startsWith("/swagger")) return true;
        if ("/actuator/health".equals(uri)) return true;
        if (uri.startsWith("/api/auth/")) return true;
        
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}
