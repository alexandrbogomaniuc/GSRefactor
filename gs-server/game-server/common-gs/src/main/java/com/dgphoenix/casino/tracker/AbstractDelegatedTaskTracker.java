package com.abs.casino.tracker;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraTrackingInfoPersister;
import com.abs.casino.common.engine.tracker.AbstractCommonTracker;
import com.abs.casino.common.engine.tracker.ICommonTrackingTaskDelegate;
import com.abs.casino.common.util.ApplicationContextHelper;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * User: Grien
 * Date: 30.04.2014 18:27
 */
public abstract class AbstractDelegatedTaskTracker<T extends ICommonTrackingTaskDelegate>
        extends AbstractCommonTracker<String, DelegatedTask> {

    private Class<T> aClass;
    private final CassandraTrackingInfoPersister trackingInfoPersister;

    protected AbstractDelegatedTaskTracker(Class<T> aClass) {
        super();
        this.aClass = aClass;
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        trackingInfoPersister = persistenceManager.getPersister(CassandraTrackingInfoPersister.class);
    }

    @Override
    protected void chargeTasks() {
        Map<String, T> teskDelegates = loadChargedTasks(trackingInfoPersister);
        for (Map.Entry<String, T> taskDelegate : teskDelegates.entrySet()) {
            DelegatedTask task = new DelegatedTask(taskDelegate.getKey(), this, taskDelegate.getValue());
            addNewTask(taskDelegate.getKey(), task, getInitialDelay());
        }
        Logger log = getLog();
        log.info("Added tracking tasks: " + teskDelegates.size());
    }

    @Override
    protected DelegatedTask createNewTask(String key) {
        T addition = trackingInfoPersister.getTrackingInfo(getUniqueTrackerName(), key, false, aClass);
        return new DelegatedTask(key, this, addition);
    }

    protected Map<String, T> loadChargedTasks(CassandraTrackingInfoPersister instance) {
        return instance.getTrackingInfo(getUniqueTrackerName(), aClass);
    }

    protected void registerForTracking(String key, T task) {
        trackingInfoPersister.persist(getUniqueTrackerName(), key, task);
    }

    public abstract String getUniqueTrackerName();

    protected abstract long getInitialDelay();
}
