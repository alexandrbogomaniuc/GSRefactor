package com.abs.casino.common.engine.tracker;

/**
 * User: plastical
 * Date: 20.05.2010
 */
public interface ICommonTracker<T> {    
    boolean isInitialized();
    void addTask(T key);
    void addTask(T key, long delayInMillis);
    void remove(T key);
}
