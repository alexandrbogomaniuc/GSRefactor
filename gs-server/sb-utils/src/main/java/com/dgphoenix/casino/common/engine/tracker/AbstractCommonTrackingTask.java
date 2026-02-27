package com.abs.casino.common.engine.tracker;

import com.dgphoenix.casino.common.exception.CommonException;
import org.apache.log4j.Logger;

/**
 * User: plastical
 * Date: 20.05.2010
 */
public abstract class AbstractCommonTrackingTask<K, T extends ICommonTracker> implements Runnable {
    private static final Logger LOG = Logger.getLogger(AbstractCommonTrackingTask.class);
    private static final long DEFAULT_TIMEOUT = 10000L;
    private static final String THREAD_NAME_DELIMITER = "#";

    private final K key;
    private final T tracker;

    protected AbstractCommonTrackingTask(K key, T tracker) {
        this.key = key;
        this.tracker = tracker;
    }

    public K getKey() {
        return key;
    }

    public T getTracker() {
        return tracker;
    }

    /**
     * Do not catch Throwable here, because ThreadDeath error could be caught
     */
    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getSimpleName() + THREAD_NAME_DELIMITER +
                String.valueOf(Thread.currentThread().getId()));

        boolean done = false;
        boolean fatalError = false;

        K key = getKey();
        try {
            process();
            done = true;
        } catch (CommonException e) {
            handleCommonException(done, fatalError, e);
        } catch (Exception e) {
            getLog().error("run key:" + key + " execution is stopped. error is:", e);
            fatalError = true;
        }

        if (done || fatalError) {
            remove(done, fatalError);
            getLog().debug("run key:" + key + " ended, done:" + done + " fatalError:" + fatalError);
        }
    }

    /**
     * Loop in this method (in case of common exception) makes possible to rerun undone task.
     * Undone task firstly removed from tracker and then added to it again
     * as a new task.
     */
    protected void handleCommonException(boolean done, boolean fatalError, CommonException ex) {
        getLog().error(this.getClass().getSimpleName() + "::run key:" + key + " error:", ex);

        remove(done, fatalError);
        try {
            getTracker().addTask(getKey(), getTaskSleepTimeout());
        } catch (CommonException e) {
            getLog().error("handleCommonException error:", e);
        } catch (Throwable e) {
            getLog().error("handleCommonException unexpected error:", e);
        }
    }

    protected void remove(boolean done, boolean fatalError) {
        getTracker().remove(getKey());
    }

    private long getSleepTimeout() {
        long timeout;
        try {
            timeout = getTaskSleepTimeout();
        } catch (CommonException e) {
            timeout = DEFAULT_TIMEOUT;
            getLog().error("getSleepTimeout error getting timeout:", e);
        }

        return timeout;
    }

    protected abstract void process() throws CommonException;

    protected abstract long getTaskSleepTimeout() throws CommonException;

    public Logger getLog() {
        return LOG;
    }
}
