package com.shmoney.auth.service;

import com.shmoney.auth.exception.InvalidTokenException;
import com.shmoney.auth.token.JwtTokenDetails;
import com.shmoney.auth.token.TokenPair;
import com.shmoney.auth.token.TokenType;
import com.shmoney.config.JwtProperties;
import com.shmoney.user.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Objects;

@Service
public class JwtTokenService {
    
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ROLE_CLAIM = "role";
    private static final String NAME_CLAIM = "name";
    
    private final JwtProperties properties;
    private final SecretKey signingKey;
    private final JwtParser parser;
    
    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.parser = Jwts.parser()
                .verifyWith(signingKey)
                .build();
    }
    
    public TokenPair generateTokenPair(User user) {
        GeneratedToken access = buildToken(user, TokenType.ACCESS, properties.accessTokenTtl());
        GeneratedToken refresh = buildToken(user, TokenType.REFRESH, properties.refreshTokenTtl());
        
        return new TokenPair(
                access.token(),
                access.expiresAt(),
                refresh.token(),
                refresh.expiresAt()
        );
    }
    
    public JwtTokenDetails parseAccessToken(String token) {
        return parseToken(token, TokenType.ACCESS);
    }
    
    public JwtTokenDetails parseRefreshToken(String token) {
        return parseToken(token, TokenType.REFRESH);
    }
    
    private GeneratedToken buildToken(User user, TokenType type, java.time.Duration ttl) {
        OffsetDateTime issuedAt = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = issuedAt.plus(ttl);
        
        String token = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(NAME_CLAIM, user.getTelegramUsername())
                .claim(ROLE_CLAIM, user.getRole())
                .claim(TOKEN_TYPE_CLAIM, type.claimValue())
                .issuedAt(Date.from(issuedAt.toInstant()))
                .expiration(Date.from(expiresAt.toInstant()))
                .signWith(signingKey)
                .compact();
        
        return new GeneratedToken(token, expiresAt);
    }
    
    private JwtTokenDetails parseToken(String token, TokenType expectedType) {
        try {
            var claimsJws = parser.parseSignedClaims(token);
            var claims = claimsJws.getPayload();
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            
            if (!Objects.equals(expectedType.claimValue(), tokenType)) {
                throw new InvalidTokenException("Unexpected token type");
            }
            
            Long userId = Long.valueOf(claims.getSubject());
            String displayName = claims.get(NAME_CLAIM, String.class);
            String role = claims.get(ROLE_CLAIM, String.class);
            OffsetDateTime expiresAt = OffsetDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneOffset.UTC);
            
            return new JwtTokenDetails(userId, displayName, role, expiresAt);
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException("Token expired", ex);
        } catch (RuntimeException ex) {
            throw new InvalidTokenException("Token is invalid", ex);
        }
    }
    
    private record GeneratedToken(String token, OffsetDateTime expiresAt) {
    }
}
