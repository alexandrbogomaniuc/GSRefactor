package com.abs.casino.leaderboard;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import com.abs.casino.tracker.AbstractDelegatedTaskTracker;
import com.abs.casino.tracker.DelegatedTask;
import org.apache.log4j.Logger;

public class LeaderboardWinTracker extends AbstractDelegatedTaskTracker<LeaderboardWinTrackerTask> {
    private static final Logger LOG = Logger.getLogger(LeaderboardWinTracker.class);
    private static final LeaderboardWinTracker instance = new LeaderboardWinTracker();
    private static final String TRACKER_NAME = "LWT";
    private static final int THREAD_POOL_SIZE = 15;
    private static final String KEY_DELIMITER = "+";

    private final GameServerConfiguration serverConfiguration;

    protected LeaderboardWinTracker() {
        super(LeaderboardWinTrackerTask.class);
        serverConfiguration = ApplicationContextHelper.getApplicationContext()
                .getBean("gameServerConfiguration", GameServerConfiguration.class);
    }

    public static LeaderboardWinTracker getInstance() {
        return instance;
    }

    @Override
    protected Logger getLog() {
        return LOG;
    }

    @Override
    protected int getThreadPoolSize() throws CommonException {
        return THREAD_POOL_SIZE;
    }

    @Override
    public String getUniqueTrackerName() {
        return TRACKER_NAME;
    }

    @Override
    protected long getInitialDelay() {
        return serverConfiguration.getLeaderboardTrackerInterval();
    }

    public void addTask(long bankId, long leaderboardId, long endDate, String result) {
        String key = bankId + KEY_DELIMITER + leaderboardId;
        LeaderboardWinTrackerTask task = new LeaderboardWinTrackerTask(bankId, leaderboardId, endDate, result);
        registerForTracking(key, task);
        super.addTask(key, new DelegatedTask(key, this, task), serverConfiguration.getLeaderboardTrackerInterval());
    }
}
