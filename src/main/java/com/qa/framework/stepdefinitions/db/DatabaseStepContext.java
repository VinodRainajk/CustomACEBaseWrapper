package com.qa.framework.stepdefinitions.db;

import com.qa.framework.db.DatabaseConnection;
import com.qa.framework.db.DatabaseManager;
import com.qa.framework.utils.CsvResultComparator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared context for all database step definitions.
 * Holds state that is shared across DatabaseConnectionStepDefinitions,
 * DatabaseSelectStepDefinitions, etc.
 */
public class DatabaseStepContext {

    private static final ThreadLocal<DatabaseStepContext> INSTANCE = ThreadLocal.withInitial(DatabaseStepContext::new);

    private DatabaseManager dbManager;
    private DatabaseConnection currentConnection;
    private String featureName;
    private String scenarioName;
    /** Results by connection name for steps like "on \"mysql\"" */
    private Map<String, List<Map<String, Object>>> resultsByConnection = new HashMap<>();
    private List<Map<String, Object>> queryResults;
    private int updateCount;
    private int deleteCount;
    private int insertCount;
    private Exception lastException;
    private Object procedureResult;
    private Object functionResult;
    private CsvComparisonState lastCsvComparisonState;

    public static final class CsvComparisonState {
        private final boolean expectedToMatch;
        private final String payloadKey;
        private final CsvResultComparator.DetailedComparisonResult result;

        public CsvComparisonState(boolean expectedToMatch, String payloadKey, CsvResultComparator.DetailedComparisonResult result) {
            this.expectedToMatch = expectedToMatch;
            this.payloadKey = payloadKey;
            this.result = result;
        }

        public boolean isExpectedToMatch() {
            return expectedToMatch;
        }

        public String getPayloadKey() {
            return payloadKey;
        }

        public CsvResultComparator.DetailedComparisonResult getResult() {
            return result;
        }
    }

    public static DatabaseStepContext getInstance() {
        return INSTANCE.get();
    }

    public static void reset() {
        INSTANCE.remove();
    }

    public DatabaseManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public DatabaseConnection getCurrentConnection() {
        return currentConnection;
    }

    public void setCurrentConnection(DatabaseConnection currentConnection) {
        this.currentConnection = currentConnection;
    }

    public List<Map<String, Object>> getQueryResults() {
        return queryResults;
    }

    public void setQueryResults(List<Map<String, Object>> queryResults) {
        this.queryResults = queryResults;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    public int getDeleteCount() {
        return deleteCount;
    }

    public void setDeleteCount(int deleteCount) {
        this.deleteCount = deleteCount;
    }

    public int getInsertCount() {
        return insertCount;
    }

    public void setInsertCount(int insertCount) {
        this.insertCount = insertCount;
    }

    public Exception getLastException() {
        return lastException;
    }

    public void setLastException(Exception lastException) {
        this.lastException = lastException;
    }

    public Object getProcedureResult() {
        return procedureResult;
    }

    public void setProcedureResult(Object procedureResult) {
        this.procedureResult = procedureResult;
    }

    public Object getFunctionResult() {
        return functionResult;
    }

    public void setFunctionResult(Object functionResult) {
        this.functionResult = functionResult;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public Map<String, List<Map<String, Object>>> getResultsByConnection() {
        return resultsByConnection;
    }

    public void putResultsForConnection(String connectionName, List<Map<String, Object>> results) {
        resultsByConnection.put(connectionName, results);
    }

    public List<Map<String, Object>> getResultsForConnection(String connectionName) {
        return resultsByConnection.get(connectionName);
    }

    public CsvComparisonState getLastCsvComparisonState() {
        return lastCsvComparisonState;
    }

    public void setLastCsvComparisonState(CsvComparisonState lastCsvComparisonState) {
        this.lastCsvComparisonState = lastCsvComparisonState;
    }
}
