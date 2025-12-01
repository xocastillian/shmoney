package db.migration;

import com.shmoney.common.crypto.EncryptionContext;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class V10__encrypt_amount_columns_data extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        encryptColumn(connection, "wallets", "id", "balance");
        encryptColumn(connection, "wallet_transactions", "id", "source_amount");
        encryptColumn(connection, "wallet_transactions", "id", "target_amount");
        encryptColumn(connection, "category_transactions", "id", "amount");
    }

    private void encryptColumn(Connection connection, String table, String idColumn, String valueColumn) throws SQLException {
        String selectSql = "SELECT " + idColumn + ", " + valueColumn + " FROM " + table;
        String updateSql = "UPDATE " + table + " SET " + valueColumn + " = ? WHERE " + idColumn + " = ?";

        try (PreparedStatement select = connection.prepareStatement(selectSql);
             PreparedStatement update = connection.prepareStatement(updateSql)) {
            ResultSet rs = select.executeQuery();
            int batchSize = 0;
            while (rs.next()) {
                long id = rs.getLong(idColumn);
                String value = rs.getString(valueColumn);
                if (value == null || value.isBlank() || EncryptionContext.isEncrypted(value)) {
                    continue;
                }
                String encrypted = EncryptionContext.encrypt(value.trim());
                update.setString(1, encrypted);
                update.setLong(2, id);
                update.addBatch();
                batchSize++;
                if (batchSize >= 500) {
                    update.executeBatch();
                    batchSize = 0;
                }
            }
            if (batchSize > 0) {
                update.executeBatch();
            }
        }
    }
}
