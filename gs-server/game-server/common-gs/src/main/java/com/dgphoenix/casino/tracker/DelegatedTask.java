package com.abs.casino.tracker;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraTrackingInfoPersister;
import com.abs.casino.common.engine.tracker.AbstractCommonTrackingTask;
import com.abs.casino.common.engine.tracker.ICommonTrackingTaskDelegate;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.ApplicationContextHelper;
import org.apache.log4j.Logger;

/**
 * User: Grien
 * Date: 02.04.2014 12:50
 */
public class DelegatedTask extends AbstractCommonTrackingTask<String, AbstractDelegatedTaskTracker> {
    private ICommonTrackingTaskDelegate delegate;
    private final CassandraTrackingInfoPersister trackingInfoPersister;

    public DelegatedTask(String key, AbstractDelegatedTaskTracker tracker,
                         ICommonTrackingTaskDelegate delegate) {
        super(key, tracker);
        this.delegate = delegate;
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        trackingInfoPersister = persistenceManager.getPersister(CassandraTrackingInfoPersister.class);
    }

    @Override
    protected void process() throws CommonException {
        delegate.process(getKey(), getTracker());
        trackingInfoPersister.delete(getTracker().getUniqueTrackerName(), getKey());
    }

    @Override
    protected long getTaskSleepTimeout() throws CommonException {
        return delegate.getTaskSleepTimeout();
    }

    @Override
    public Logger getLog() {
        return delegate.getLog();
    }
}
