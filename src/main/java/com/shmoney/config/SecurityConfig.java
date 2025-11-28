package com.shmoney.config;

import com.shmoney.auth.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(loggingAuthEntryPoint())
                        .accessDeniedHandler(loggingAccessDeniedHandler())
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger", "/swagger/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration config = new CorsConfiguration();
        
        if (corsProperties.allowedOrigins() != null && !corsProperties.allowedOrigins().isEmpty()) {
            corsProperties.allowedOrigins().forEach(config::addAllowedOriginPattern);
        } else {
            config.addAllowedOriginPattern("*");
        }
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        config.setAllowCredentials(corsProperties.allowCredentials());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return source;
    }
    
    @Bean
    public AuthenticationEntryPoint loggingAuthEntryPoint() {
        return (request, response, authException) -> {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);
            logger.warn("401 Unauthorized for {} {}: {}", request.getMethod(), request.getRequestURI(),
                    authException.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        };
    }
    
    @Bean
    public AccessDeniedHandler loggingAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);
            logger.warn("403 AccessDenied for {} {}: {}", request.getMethod(), request.getRequestURI(),
                    accessDeniedException.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        };
    }
}
