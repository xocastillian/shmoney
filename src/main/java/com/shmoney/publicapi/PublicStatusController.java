package com.shmoney.publicapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public status")
@RestController
@RequestMapping("/api/public")
public class PublicStatusController {

    private final Instant startedAt = Instant.now();

    @Operation(summary = "Проверка доступности API без авторизации")
    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Instant now = Instant.now();
        long uptimeSeconds = Duration.between(startedAt, now).getSeconds();

        return Map.of(
                "status", "ok",
                "timestamp", now.toString(),
                "uptimeSeconds", uptimeSeconds
        );
    }
}
