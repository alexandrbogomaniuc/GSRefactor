package com.abs.casino.common.engine.tracker;

import com.abs.casino.common.exception.CommonException;
import org.apache.log4j.Logger;

/**
 * User: plastical
 * Date: 08.06.2010
 */
public class DelayedExecutor extends AbstractCommonTracker {
    private static final Logger LOG = Logger.getLogger(DelayedExecutor.class);
    private static final DelayedExecutor instance = new DelayedExecutor();

    public static DelayedExecutor getInstance() {
        return instance;
    }

    private DelayedExecutor() {
    }

    @Override
    protected Logger getLog() {
        return LOG;
    }

    @Override
    protected void chargeTasks() {
        //nothing to implement here. Do not throw exception!!!
    }

    @Override
    protected AbstractCommonTrackingTask createNewTask(Object key) {
        throw new RuntimeException("this method is not available");
    }

    @Override
    protected int getThreadPoolSize() throws CommonException {
        return 15;
    }
}
