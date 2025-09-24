package com.shmoney.user.dto;

public record TelegramUserData(
        Long id,
        String username,
        String firstName,
        String lastName,
        String languageCode
) {
}
