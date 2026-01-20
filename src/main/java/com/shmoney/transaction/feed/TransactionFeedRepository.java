package com.shmoney.transaction.feed;

import com.shmoney.common.crypto.EncryptedBigDecimalConverter;
import com.shmoney.debt.entity.DebtTransactionDirection;
import com.shmoney.transaction.category.entity.CategoryTransactionType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.sql.Types;
import java.util.List;

@Repository
public class TransactionFeedRepository {

    private static final String BASE_FILTER = """
            FROM user_transaction_feed
            WHERE user_id = :userId
              AND (:type = 'ALL' OR (
                    :type = 'TRANSFER' AND entry_source = 'TRANSFER'
                ) OR (
                    :type IN ('EXPENSE','INCOME') AND entry_source = 'CATEGORY' AND category_transaction_type = :type
                ) OR (
                    :type = 'DEBT' AND entry_source = 'DEBT'
                ))
              AND ((:fromDate)::TIMESTAMPTZ IS NULL OR occurred_at >= (:fromDate)::TIMESTAMPTZ)
              AND ((:toDate)::TIMESTAMPTZ IS NULL OR occurred_at <= (:toDate)::TIMESTAMPTZ)
            """;

    private static final String ROW_SQL_PREFIX = """
            SELECT entry_id,
                   entry_source,
                   category_transaction_type,
                   wallet_id,
                   counterparty_wallet_id,
                   category_id,
                   debt_counterparty_id,
                   debt_direction,
                   amount,
                   currency_code,
                   description,
                   occurred_at,
                   created_at
            """ + BASE_FILTER;

    private static final String COUNT_SQL_PREFIX = "SELECT COUNT(*) " + BASE_FILTER;

    private static final String ORDER_LIMIT = """
            ORDER BY occurred_at DESC, entry_id DESC
            OFFSET :offset LIMIT :limit
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TransactionFeedRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PagedFeedResult fetch(Long userId,
                                 TransactionFeedType type,
                                 List<Long> walletIds,
                                 List<Long> categoryIds,
                                 List<Long> debtCounterpartyIds,
                                 DebtTransactionDirection debtDirection,
                                 OffsetDateTime from,
                                 OffsetDateTime to,
                                 int page,
                                 int size) {
        int limit = Math.min(size <= 0 ? 50 : size, 100);
        int currentPage = Math.max(page, 0);
        int offset = currentPage * limit;

        MapSqlParameterSource params = buildParams(userId, type, debtDirection, from, to)
                .addValue("offset", offset)
                .addValue("limit", limit);

        addOptionalFilterValues(params, "walletIds", walletIds);
        addOptionalFilterValues(params, "categoryIds", categoryIds);
        addOptionalFilterValues(params, "debtCounterpartyIds", debtCounterpartyIds);

        String filters = buildAdditionalFilters(walletIds, categoryIds, debtCounterpartyIds, debtDirection);
        String rowSql = ROW_SQL_PREFIX + filters + ORDER_LIMIT;
        String countSql = COUNT_SQL_PREFIX + filters;

        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        long totalCount = total == null ? 0 : total;
        List<TransactionFeedItem> items = jdbcTemplate.query(rowSql, params, new TransactionFeedRowMapper());
        return new PagedFeedResult(totalCount, currentPage, limit, items);
    }

    private MapSqlParameterSource buildParams(Long userId,
                                              TransactionFeedType type,
                                              DebtTransactionDirection debtDirection,
                                              OffsetDateTime from,
                                              OffsetDateTime to) {
        return new MapSqlParameterSource()
                .addValue("userId", userId, Types.BIGINT)
                .addValue("type", type == null ? "ALL" : type.name(), Types.VARCHAR)
                .addValue("debtDirection", debtDirection == null ? null : debtDirection.name(), Types.VARCHAR)
                .addValue("fromDate", toTimestamp(from), Types.TIMESTAMP)
                .addValue("toDate", toTimestamp(to), Types.TIMESTAMP);
    }

    private void addOptionalFilterValues(MapSqlParameterSource params, String name, List<Long> values) {
        if (values != null && !values.isEmpty()) {
            params.addValue(name, values);
        }
    }

    private String buildAdditionalFilters(List<Long> walletIds,
                                          List<Long> categoryIds,
                                          List<Long> debtCounterpartyIds,
                                          DebtTransactionDirection debtDirection) {
        StringBuilder builder = new StringBuilder();
        if (walletIds != null && !walletIds.isEmpty()) {
            builder.append("""
                    AND EXISTS (
                        SELECT 1
                        FROM unnest(wallet_ids) AS wallet_filter(wallet_id)
                        WHERE wallet_id IN (:walletIds)
                    )
                    """);
        }
        if (categoryIds != null && !categoryIds.isEmpty()) {
            builder.append("""
                    AND entry_source = 'CATEGORY'
                    AND category_id IN (:categoryIds)
                    """);
        }
        if (debtCounterpartyIds != null && !debtCounterpartyIds.isEmpty()) {
            builder.append("""
                    AND entry_source = 'DEBT'
                    AND debt_counterparty_id IN (:debtCounterpartyIds)
                    """);
        }
        if (debtDirection != null) {
            builder.append("""
                    AND entry_source = 'DEBT'
                    AND debt_direction = :debtDirection
                    """);
        }
        return builder.toString();
    }

    private Timestamp toTimestamp(OffsetDateTime dateTime) {
        return dateTime == null ? null : Timestamp.from(dateTime.toInstant());
    }

    private static class TransactionFeedRowMapper implements RowMapper<TransactionFeedItem> {
        @Override
        public TransactionFeedItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            String entrySource = rs.getString("entry_source");
            String categoryType = rs.getString("category_transaction_type");
            CategoryTransactionType mappedType = categoryType == null || "TRANSFER".equals(categoryType)
                    ? null
                    : CategoryTransactionType.valueOf(categoryType);
            BigDecimal amount = EncryptedBigDecimalConverter.decryptValue(rs.getString("amount"));
            return new TransactionFeedItem(
                    rs.getLong("entry_id"),
                    entrySource,
                    mappedType,
                    rs.getObject("wallet_id", Long.class),
                    rs.getObject("counterparty_wallet_id", Long.class),
                    rs.getObject("category_id", Long.class),
                    rs.getObject("debt_counterparty_id", Long.class),
                    rs.getString("debt_direction"),
                    amount,
                    rs.getString("currency_code"),
                    rs.getString("description"),
                    rs.getObject("occurred_at", OffsetDateTime.class),
                    rs.getObject("created_at", OffsetDateTime.class)
            );
        }
    }

    public record PagedFeedResult(long totalCount, int page, int size, List<TransactionFeedItem> items) {
    }
}
