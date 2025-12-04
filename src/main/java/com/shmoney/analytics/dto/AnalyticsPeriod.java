package com.shmoney.analytics.dto;

import java.time.OffsetDateTime;

public record AnalyticsPeriod(
        OffsetDateTime from,
        OffsetDateTime to
) {
}
