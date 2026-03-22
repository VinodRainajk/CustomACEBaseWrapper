package com.qa.framework.db;

import com.qa.framework.exceptions.WrapperException;
import com.qa.framework.payload.PayloadStepContext;
import com.qa.framework.stepdefinitions.db.DatabaseStepContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Single place for executing SQL from {@link PayloadStepContext#getPendingSqlStatement()} or
 * {@link PayloadStepContext#getPendingPreparedStatement()}.
 * Used by DatabaseSelectStepDefinitions, DatabaseInsertStepDefinitions, etc.
 */
public final class PendingStatementExecutor {

    private PendingStatementExecutor() {
    }

    /**
     * Execute pending SQL with the given operation type. Clears pending SQL after success.
     *
     * @param ctx           database step context
     * @param operation     "select", "insert", "update", "delete"
     * @param connectionName optional; if null, uses current connection
     */
    public static void executePendingStatement(DatabaseStepContext ctx, String operation, String connectionName) {
        String sql = PayloadStepContext.getPendingSqlStatement();
        if (sql == null) {
            ctx.setLastException(new WrapperException(
                    "No SQL statement was set. First use: When I set the SQL statement from feature payload \"<key>\""));
            return;
        }
        try {
            DatabaseConnection conn = resolveConnection(ctx, connectionName);
            assertNotNull(conn, "No connection found" + (connectionName != null ? " for: " + connectionName : ""));

            switch (operation.toLowerCase()) {
                case "select":
                    List<Map<String, Object>> results = conn.executeQuery(sql);
                    ctx.setQueryResults(results);
                    if (connectionName != null) {
                        ctx.putResultsForConnection(connectionName, results);
                        ctx.setCurrentConnection(conn);
                    }
                    break;
                case "insert":
                case "update":
                case "delete":
                    int count = conn.executeUpdate(sql);
                    ctx.setUpdateCount(count);
                    ctx.setInsertCount("insert".equalsIgnoreCase(operation) ? count : ctx.getInsertCount());
                    ctx.setDeleteCount("delete".equalsIgnoreCase(operation) ? count : ctx.getDeleteCount());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operation: " + operation);
            }
            ctx.setLastException(null);
            PayloadStepContext.clearPendingSqlStatement();
        } catch (Exception e) {
            ctx.setLastException(e);
        }
    }

    /**
     * Execute pending prepared statement (query + parameters). Clears after success.
     */
    public static void executePendingPreparedStatement(DatabaseStepContext ctx, String connectionName) {
        PayloadStepContext.PreparedPayload prepared = PayloadStepContext.getPendingPreparedStatement();
        if (prepared == null) {
            ctx.setLastException(new WrapperException(
                    "No prepared statement was set. First use: When I set the prepared statement from feature payload \"<key>\""));
            return;
        }
        try {
            DatabaseConnection conn = resolveConnection(ctx, connectionName);
            assertNotNull(conn, "No connection found" + (connectionName != null ? " for: " + connectionName : ""));

            List<Map<String, Object>> results = conn.executePreparedQuery(
                    prepared.getQuery(),
                    prepared.getParameters() != null ? prepared.getParameters().toArray() : new Object[0]);
            ctx.setQueryResults(results);
            if (connectionName != null) {
                ctx.putResultsForConnection(connectionName, results);
                ctx.setCurrentConnection(conn);
            }
            ctx.setLastException(null);
            PayloadStepContext.clearPendingPreparedStatement();
        } catch (Exception e) {
            ctx.setLastException(e);
        }
    }

    private static DatabaseConnection resolveConnection(DatabaseStepContext ctx, String connectionName) {
        return connectionName != null && !connectionName.isEmpty()
                ? ctx.getDbManager().getConnection(connectionName)
                : ctx.getCurrentConnection();
    }
}
