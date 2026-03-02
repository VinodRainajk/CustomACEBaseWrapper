package com.qa.framework.base;

/**
 * Base wrapper class providing common functionality for QA Framework.
 * This class serves as a foundation for creating custom wrappers.
 */
public abstract class BaseWrapper {
    
    protected String name;
    protected boolean initialized;
    
    public BaseWrapper(String name) {
        this.name = name;
        this.initialized = false;
    }
    
    /**
     * Initialize the wrapper with default configuration.
     */
    public void initialize() {
        if (!initialized) {
            doInitialize();
            initialized = true;
        }
    }
    
    /**
     * Template method for subclasses to implement initialization logic.
     */
    protected abstract void doInitialize();
    
    /**
     * Clean up resources and reset the wrapper.
     */
    public void cleanup() {
        if (initialized) {
            doCleanup();
            initialized = false;
        }
    }
    
    /**
     * Template method for subclasses to implement cleanup logic.
     */
    protected abstract void doCleanup();
    
    /**
     * Check if the wrapper is initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Get the name of this wrapper.
     * 
     * @return the wrapper name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the name of this wrapper.
     * 
     * @param name the new wrapper name
     */
    public void setName(String name) {
        this.name = name;
    }
}



