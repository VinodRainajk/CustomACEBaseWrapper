package com.qa.framework.payload;

import java.util.List;

/**
 * Thread-local holder for content loaded from feature payload YAML, reused across steps
 * (e.g. API request body set in one step, consumed by POST/PUT/PATCH in the next;
 * SQL statement set in one step, consumed by execute query/insert/update/delete in the next).
 */
public final class PayloadStepContext {

    private static final ThreadLocal<String> PENDING_BODY = new ThreadLocal<>();
    private static final ThreadLocal<String> PENDING_SQL = new ThreadLocal<>();
    private static final ThreadLocal<PreparedPayload> PENDING_PREPARED = new ThreadLocal<>();

    private PayloadStepContext() {
    }

    /** JSON (or other scalar text) to send as the next API body, etc. */
    public static void setPendingBody(String body) {
        PENDING_BODY.set(body);
    }

    public static String getPendingBody() {
        return PENDING_BODY.get();
    }

    public static void clearPendingBody() {
        PENDING_BODY.remove();
    }

    /** SQL statement for the next execute query/insert/update/delete step. */
    public static void setPendingSqlStatement(String sql) {
        PENDING_SQL.set(sql);
    }

    public static String getPendingSqlStatement() {
        return PENDING_SQL.get();
    }

    public static void clearPendingSqlStatement() {
        PENDING_SQL.remove();
    }

    /** Prepared statement (query + parameters) for the next execute prepared query step. */
    public static void setPendingPreparedStatement(String query, List<String> parameters) {
        PENDING_PREPARED.set(new PreparedPayload(query, parameters));
    }

    public static PreparedPayload getPendingPreparedStatement() {
        return PENDING_PREPARED.get();
    }

    public static void clearPendingPreparedStatement() {
        PENDING_PREPARED.remove();
    }

    public static final class PreparedPayload {
        private final String query;
        private final List<String> parameters;

        public PreparedPayload(String query, List<String> parameters) {
            this.query = query;
            this.parameters = parameters;
        }

        public String getQuery() {
            return query;
        }

        public List<String> getParameters() {
            return parameters;
        }
    }
}
