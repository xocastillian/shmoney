package com.shmoney.transaction.feed;

import java.time.OffsetDateTime;

public enum TransactionFeedPeriod {
    TODAY {
        @Override
        OffsetDateTime calculateFrom(OffsetDateTime now) {
            return now.toLocalDate().atStartOfDay(now.getOffset()).toOffsetDateTime();
        }
    },
    LAST_7_DAYS {
        @Override
        OffsetDateTime calculateFrom(OffsetDateTime now) {
            return now.minusDays(7);
        }
    },
    LAST_MONTH {
        @Override
        OffsetDateTime calculateFrom(OffsetDateTime now) {
            return now.minusMonths(1);
        }
    },
    LAST_6_MONTHS {
        @Override
        OffsetDateTime calculateFrom(OffsetDateTime now) {
            return now.minusMonths(6);
        }
    },
    LAST_YEAR {
        @Override
        OffsetDateTime calculateFrom(OffsetDateTime now) {
            return now.minusYears(1);
        }
    };
    
    public DateRange resolve(OffsetDateTime now) {
        OffsetDateTime safeNow = now == null ? OffsetDateTime.now() : now;
        return new DateRange(calculateFrom(safeNow), safeNow);
    }
    
    abstract OffsetDateTime calculateFrom(OffsetDateTime now);
    
    public record DateRange(OffsetDateTime from, OffsetDateTime to) {
    }
}
