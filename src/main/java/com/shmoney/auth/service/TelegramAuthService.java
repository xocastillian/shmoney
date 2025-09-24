package com.shmoney.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shmoney.auth.dto.AuthResponse;
import com.shmoney.auth.dto.TelegramAuthRequest;
import com.shmoney.auth.exception.TelegramAuthenticationException;
import com.shmoney.auth.token.TokenPair;
import com.shmoney.config.TelegramProperties;
import com.shmoney.user.dto.TelegramUserData;
import com.shmoney.user.entity.User;
import com.shmoney.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class TelegramAuthService {
    
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String HASH_FIELD = "hash";
    private static final String USER_FIELD = "user";
    private static final String AUTH_DATE_FIELD = "auth_date";
    private static final String WEBAPP_DATA = "WebAppData";
    
    private final TelegramProperties properties;
    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(TelegramAuthService.class);
    
    public TelegramAuthService(TelegramProperties properties,
                               UserService userService,
                               JwtTokenService jwtTokenService,
                               ObjectMapper objectMapper) {
        this.properties = properties;
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.objectMapper = objectMapper;
    }
    
    public AuthResponse authenticate(TelegramAuthRequest request) {
        String botToken = ensureEnabled();
        InitData initData = parseInitData(request.initData());
        
        validateSignature(initData, botToken);
        ensureNotExpired(initData.values().get(AUTH_DATE_FIELD));
        
        TelegramUserPayload payload = parseUserPayload(initData.values().get(USER_FIELD));
        
        TelegramUserData telegramUser = new TelegramUserData(
                payload.id(),
                payload.username(),
                payload.firstName(),
                payload.lastName(),
                payload.languageCode()
        );
        
        User user = userService.syncTelegramUser(telegramUser, properties.defaultRole());
        TokenPair tokenPair = jwtTokenService.generateTokenPair(user);
        
        return new AuthResponse(
                tokenPair.accessToken(),
                tokenPair.accessTokenExpiresAt(),
                tokenPair.refreshToken(),
                tokenPair.refreshTokenExpiresAt()
        );
    }
    
    private String ensureEnabled() {
        if (!properties.enabled()) {
            throw new TelegramAuthenticationException("Авторизация через Telegram отключена");
        }
        
        if (!StringUtils.hasText(properties.botToken())) {
            throw new TelegramAuthenticationException("Не указан токен Telegram-бота");
        }
        
        String token = properties.botToken().trim();
        String suffix = token.length() > 4 ? token.substring(token.length() - 4) : token;
        
        logger.info("Using Telegram bot token (length={}, last4=****{} )", token.length(), suffix);
        
        return token;
    }
    
    private InitData parseInitData(String initData) {
        if (!StringUtils.hasText(initData)) {
            throw new TelegramAuthenticationException("initData не может быть пустым");
        }
        
        Map<String, String> raw = new HashMap<>();
        Map<String, String> decoded = new HashMap<>();
        
        for (String pair : initData.split("&")) {
            if (!StringUtils.hasText(pair)) continue;
            
            int idx = pair.indexOf('=');
            
            if (idx <= 0) continue;
            
            String keyRaw = pair.substring(0, idx);
            String valueRaw = pair.substring(idx + 1);
            
            raw.put(keyRaw, valueRaw);
            
            String key = URLDecoder.decode(keyRaw, StandardCharsets.UTF_8);
            String value = URLDecoder.decode(valueRaw, StandardCharsets.UTF_8);
            
            decoded.put(key, value);
        }
        
        return new InitData(raw, decoded);
    }
    
    private String buildDataCheckString(Map<String, String> map) {
        return map.entrySet().stream()
                .filter(entry -> !HASH_FIELD.equals(entry.getKey()))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n"));
    }
    
    private byte[] hmacSHA256(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            
            return mac.doFinal(data);
        } catch (GeneralSecurityException ex) {
            throw new TelegramAuthenticationException("HMAC_SHA256 failure", ex);
        }
    }
    
    private byte[] webAppSecretKey(String botToken) {
        return hmacSHA256(
                botToken.getBytes(StandardCharsets.UTF_8),
                WEBAPP_DATA.getBytes(StandardCharsets.UTF_8)
        );
    }
    
    private void validateSignature(InitData initData, String botToken) {
        String provided = initData.values().get(HASH_FIELD);
        byte[] secret = webAppSecretKey(botToken);
        
        String decodedString = buildDataCheckString(initData.values());
        String rawString = buildDataCheckString(initData.raw());
        
        String calculatedDecoded = toHex(hmacSHA256(secret, decodedString.getBytes(StandardCharsets.UTF_8)));
        String calculatedRaw = toHex(hmacSHA256(secret, rawString.getBytes(StandardCharsets.UTF_8)));
        
        logger.info("dataCheckString(decoded)=\n{}", decodedString);
        logger.info("dataCheckString(raw)=\n{}", rawString);
        logger.info("webAppSecretKey={}", toHex(secret));
        logger.info("calculated(decoded)={}, calculated(raw)={}, provided={}",
                calculatedDecoded, calculatedRaw, provided);
        
        if (!equalsConstTime(calculatedDecoded, provided) && !equalsConstTime(calculatedRaw, provided)) {
            throw new TelegramAuthenticationException("Неверная сигнатура initData");
        }
    }
    
    private void ensureNotExpired(String authDateRaw) {
        if (!StringUtils.hasText(authDateRaw)) {
            throw new TelegramAuthenticationException("Поле auth_date отсутствует в initData");
        }
        
        long authDate;
        
        try {
            authDate = Long.parseLong(authDateRaw);
        } catch (NumberFormatException ex) {
            throw new TelegramAuthenticationException("Некорректное значение auth_date", ex);
        }
        
        Instant authInstant = Instant.ofEpochSecond(authDate);
        Duration maxAge = properties.initDataMaxAge();
        
        if (maxAge != null && Instant.now().isAfter(authInstant.plus(maxAge))) {
            throw new TelegramAuthenticationException("initData просрочено");
        }
    }
    
    private TelegramUserPayload parseUserPayload(String userJson) {
        if (!StringUtils.hasText(userJson)) {
            throw new TelegramAuthenticationException("Данные пользователя отсутствуют в initData");
        }
        
        try {
            TelegramUserPayload payload = objectMapper.readValue(userJson, TelegramUserPayload.class);
            
            if (payload.id() == null) {
                throw new TelegramAuthenticationException("Telegram user id отсутствует");
            }
            
            return payload;
        } catch (IOException ex) {
            throw new TelegramAuthenticationException("Не удалось разобрать данные пользователя Telegram", ex);
        }
    }
    
    private boolean equalsConstTime(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        
        int r = 0;
        
        for (int i = 0; i < a.length(); i++) r |= a.charAt(i) ^ b.charAt(i);
        
        return r == 0;
    }
    
    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        
        for (byte x : bytes) sb.append(String.format("%02x", x));
        
        return sb.toString();
    }
    
    private record InitData(Map<String, String> raw, Map<String, String> decoded) {
        Map<String, String> values() {
            return decoded;
        }
    }
    
    private record TelegramUserPayload(
            @JsonProperty("id") Long id,
            @JsonProperty("username") String username,
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName,
            @JsonProperty("language_code") String languageCode
    ) {
    }
}
