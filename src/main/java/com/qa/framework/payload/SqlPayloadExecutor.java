package com.qa.framework.payload;

import com.qa.framework.db.DatabaseConnection;
import com.qa.framework.exceptions.WrapperException;
import com.qa.framework.stepdefinitions.db.DatabaseStepContext;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Loads SQL from file, resolves placeholders, executes on a named connection.
 */
public final class SqlPayloadExecutor {

    private SqlPayloadExecutor() {
    }

    public static void execute(String operationId, String connectionName) {
        DatabaseStepContext ctx = DatabaseStepContext.getInstance();
        try {
            PayloadOperation op = PayloadRegistry.getOperation(operationId);
            if (op.getType() != PayloadOperationType.SQL) {
                throw new WrapperException("Operation " + operationId + " is not type SQL");
            }
            String sql = PlaceholderResolver.resolveText(PayloadRegistry.readClasspathText(op.getFile()));
            DatabaseConnection conn = resolveConnection(ctx, connectionName);
            assertNotNull(conn, "No connection found for: " + connectionName);
            if (!conn.isConnected()) {
                conn.connect();
            }

            String trimmed = sql.trim().toUpperCase(Locale.ROOT);
            if (trimmed.startsWith("SELECT") || trimmed.startsWith("WITH")) {
                List<Map<String, Object>> results = conn.executeQuery(sql);
                ctx.setQueryResults(results);
                ctx.putResultsForConnection(connectionName, results);
                ctx.setCurrentConnection(conn);
            } else {
                int count = conn.executeUpdate(sql);
                ctx.setUpdateCount(count);
                if (trimmed.startsWith("INSERT")) {
                    ctx.setInsertCount(count);
                } else if (trimmed.startsWith("DELETE")) {
                    ctx.setDeleteCount(count);
                }
            }
            ctx.setLastException(null);
        } catch (Exception e) {
            ctx.setLastException(e);
        }
    }

    private static DatabaseConnection resolveConnection(DatabaseStepContext ctx, String connectionName) {
        if (connectionName == null || connectionName.isBlank()) {
            return ctx.getCurrentConnection();
        }
        if (ctx.getDbManager() == null) {
            throw new WrapperException("DatabaseManager not initialized. Run database @Before hook.");
        }
        return ctx.getDbManager().getConnection(connectionName);
    }
}
