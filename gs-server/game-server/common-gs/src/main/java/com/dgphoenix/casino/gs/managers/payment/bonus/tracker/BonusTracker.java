package com.abs.casino.gs.managers.payment.bonus.tracker;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraTrackingInfoPersister;
import com.abs.casino.common.engine.tracker.AbstractCommonTracker;
import com.abs.casino.common.engine.tracker.TrackingInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.log4j.Logger;

import java.util.List;


public class BonusTracker extends AbstractCommonTracker<Long, BonusTrackerTask> {
    private static final Logger LOG = Logger.getLogger(BonusTracker.class);
    public static final String TRACKER_NAME = "BT";
    private static final long START_PAUSE = 5000;
    private static final BonusTracker instance = new BonusTracker();

    private final CassandraTrackingInfoPersister trackingInfoPersister;

    public BonusTracker() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        trackingInfoPersister = persistenceManager.getPersister(CassandraTrackingInfoPersister.class);
    }

    public static BonusTracker getInstance() {
        return instance;
    }

    @Override
    protected void chargeTasks() {
        List<TrackingInfo> list = trackingInfoPersister.getList(TRACKER_NAME);

        for (TrackingInfo trackingInfo : list) {
            addNewTask(trackingInfo.getTrackingObjectId(), START_PAUSE);
        }
        LOG.info("Added tracking tasks: " + list.size());
    }

    @Override
    protected BonusTrackerTask createNewTask(Long bonusId) {
        return new BonusTrackerTask(bonusId, this);
    }

    @Override
    protected Logger getLog() {
        return LOG;
    }

    @Override
    protected int getThreadPoolSize() throws CommonException {
        return GameServerConfiguration.getInstance().getBonusTrackerThreadPoolSize();
    }
}