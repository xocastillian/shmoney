package com.shmoney.analytics.controller;

import com.shmoney.analytics.dto.AnalyticsResponse;
import com.shmoney.analytics.service.AnalyticsService;
import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@Tag(name = "Analytics")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CurrentUserProvider currentUserProvider;

    public AnalyticsController(AnalyticsService analyticsService,
                               CurrentUserProvider currentUserProvider) {
        this.analyticsService = analyticsService;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Аналитика доходов и расходов")
    @GetMapping
    public AnalyticsResponse getAnalytics(@RequestParam(required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                          OffsetDateTime from,
                                          @RequestParam(required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                          OffsetDateTime to,
                                          @RequestParam(required = false, name = "categoryIds")
                                          List<Long> categoryIds) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        return analyticsService.getAnalytics(current.id(), from, to, categoryIds);
    }
}
