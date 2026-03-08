package com.qa.framework.stepdefinitions.db;

import com.qa.framework.config.ConfigurationManager;
import com.qa.framework.db.DatabaseConnection;
import com.qa.framework.db.DatabaseManager;

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
    private ConfigurationManager configManager;
    private DatabaseConnection currentConnection;
    private List<Map<String, Object>> queryResults;
    private int updateCount;
    private int deleteCount;
    private int insertCount;
    private Exception lastException;
    private Object procedureResult;
    private Object functionResult;

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

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigurationManager configManager) {
        this.configManager = configManager;
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
}
