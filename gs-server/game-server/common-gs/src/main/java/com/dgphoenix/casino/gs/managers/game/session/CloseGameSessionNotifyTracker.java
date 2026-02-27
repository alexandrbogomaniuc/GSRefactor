package com.dgphoenix.casino.gs.managers.game.session;

import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.tracker.AbstractDelegatedTaskTracker;
import com.abs.casino.tracker.DelegatedTask;
import org.apache.log4j.Logger;


/**
 * Created by quant on 13.04.16.
 */
public class CloseGameSessionNotifyTracker extends AbstractDelegatedTaskTracker<CloseGameSessionNotifyTask> {
    private static final Logger LOG = Logger.getLogger(CloseGameSessionNotifyTracker.class);
    private static final CloseGameSessionNotifyTracker instance = new CloseGameSessionNotifyTracker();
    private static final String NAME = "CGSN";
    private static final long INITIAL_DELAY = 0;
    private final int threadPoolSize = 15;

    public static CloseGameSessionNotifyTracker getInstance() {
        return instance;
    }

    private CloseGameSessionNotifyTracker() {
        super(CloseGameSessionNotifyTask.class);
    }

    @Override
    public String getUniqueTrackerName() {
        return NAME;
    }

    @Override
    protected long getInitialDelay() {
        return INITIAL_DELAY;
    }

    @Override
    protected Logger getLog() {
        return LOG;
    }

    @Override
    protected int getThreadPoolSize() throws CommonException {
        return threadPoolSize;
    }

    public void addTask(CloseGameSessionNotifyRequest request) {
        String trackingObjectId = String.valueOf(request.getGameSessionId());
        CloseGameSessionNotifyTask task = new CloseGameSessionNotifyTask(request);
        registerForTracking(trackingObjectId, task);
        super.addTask(trackingObjectId, new DelegatedTask(trackingObjectId, this, task), 0);
    }

    public void addTask(CloseGameSessionNotifyRequest request, Long period, Long frequency) {
        String trackingObjectId = String.valueOf(request.getGameSessionId());
        CloseGameSessionNotifyTask task = new CloseGameSessionNotifyTask(request, period, frequency);
        registerForTracking(trackingObjectId, task);
        super.addTask(trackingObjectId, new DelegatedTask(trackingObjectId, this, task), 0);
    }
}
