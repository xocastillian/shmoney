package com.shmoney.auth.service;

import com.shmoney.auth.dto.AuthResponse;
import com.shmoney.auth.dto.RefreshRequest;
import com.shmoney.auth.exception.InvalidRefreshTokenException;
import com.shmoney.auth.exception.InvalidTokenException;
import com.shmoney.auth.token.TokenPair;
import com.shmoney.user.entity.User;
import com.shmoney.user.exception.UserNotFoundException;
import com.shmoney.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserService userService, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest request) {
        Long userId;

        try {
            userId = jwtTokenService.parseRefreshToken(request.refreshToken()).userId();
        } catch (InvalidTokenException ex) {
            throw new InvalidRefreshTokenException();
        }

        User user;

        try {
            user = userService.getById(userId);
        } catch (UserNotFoundException ex) {
            throw new InvalidRefreshTokenException();
        }

        TokenPair tokens = jwtTokenService.generateTokenPair(user);
        return new AuthResponse(
                tokens.accessToken(),
                tokens.accessTokenExpiresAt(),
                tokens.refreshToken(),
                tokens.refreshTokenExpiresAt()
        );
    }
}
