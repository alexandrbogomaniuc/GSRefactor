package com.abs.casino.leaderboard;

import com.dgphoenix.casino.common.engine.tracker.AbstractCommonTracker;
import com.dgphoenix.casino.common.engine.tracker.ICommonTrackingTaskDelegate;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LeaderboardWinTrackerTask implements ICommonTrackingTaskDelegate {
    private static final byte VERSION = 0;
    private static final Logger LOG = LogManager.getLogger(LeaderboardWinTrackerTask.class);
    private long bankId;
    private long leaderboardId;
    private long endDate;
    private String result;
    private long trackingStartTime;

    public LeaderboardWinTrackerTask() {}

    public LeaderboardWinTrackerTask(long bankId, long leaderboardId, long endDate, String result) {
        this.bankId = bankId;
        this.leaderboardId = leaderboardId;
        this.endDate = endDate;
        this.result = result;
        this.trackingStartTime = System.currentTimeMillis();
    }

    @Override
    public void process(String key, AbstractCommonTracker tracker) throws CommonException {
        if (System.currentTimeMillis() - trackingStartTime > GameServerConfiguration.getInstance().getLeaderboardTrackerExpireTime()) {
            LOG.info("Removing expired task: " + key);
            return;
        }
        LOG.info("Starting upload of Leaderboard result for bankId=" + bankId + " and leaderboardId=" + leaderboardId);
        boolean success = false;
        try {
            success = new LeaderboardWinUploader().upload(bankId, leaderboardId, result);
        } catch (Exception e) {
            LOG.error("Failed to upload leaderboard result for bankId=" + bankId + " and leaderboardId=" + leaderboardId, e);
        }
        if (!success) {
            throw new CommonException("Upload failed");
        }
    }

    @Override
    public long getTaskSleepTimeout() throws CommonException {
        return GameServerConfiguration.getInstance().getLeaderboardTrackerInterval();
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(bankId, true);
        output.writeLong(leaderboardId, true);
        output.writeLong(endDate, true);
        output.writeLong(trackingStartTime, true);
        output.writeString(result);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        bankId = input.readLong(true);
        leaderboardId = input.readLong(true);
        endDate = input.readLong(true);
        trackingStartTime = input.readLong(true);
        result = input.readString();
    }
}
