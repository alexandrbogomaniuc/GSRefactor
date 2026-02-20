package com.dgphoenix.casino.common;

import com.dgphoenix.casino.common.cache.data.game.GameMode;

import java.util.Date;

/**
 * Created by mic on 19.02.15.
 */
public class OnlineStatistics {
    private final long bankId;
    private final GameMode mode;
    private final long currentOnline;
    private long date;
    private long maxOnline;
    private long minOnline;

    public OnlineStatistics(long bankId, GameMode mode, long currentOnline) {
        this.bankId = bankId;
        this.mode = mode;
        this.currentOnline = currentOnline;
    }

    public OnlineStatistics(long bankId, GameMode mode, long currentOnline, long date, long maxOnline, long minOnline) {
        this.bankId = bankId;
        this.mode = mode;
        this.currentOnline = currentOnline;
        this.date = date;
        this.maxOnline = maxOnline;
        this.minOnline = minOnline;
    }

    public long getBankId() {
        return bankId;
    }

    public GameMode getMode() {
        return mode;
    }

    public long getDate() {
        return date;
    }

    public long getCurrentOnline() {
        return currentOnline;
    }

    public long getMaxOnline() {
        return maxOnline;
    }

    public long getMinOnline() {
        return minOnline;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setMaxOnline(long maxOnline) {
        this.maxOnline = maxOnline;
    }

    public void setMinOnline(long minOnline) {
        this.minOnline = minOnline;
    }

    public boolean isFirstTimeProcessing() {
        return date == 0;
    }

    public void updateMinAndMax() {
        maxOnline = Math.max(currentOnline, maxOnline);
        minOnline = Math.min(currentOnline, minOnline);
    }

    public void rollNextPeriod(long currentPeriod) {
        date = currentPeriod;
        maxOnline = minOnline = currentOnline;
    }

    @Override
    public String toString() {
        return "OnlineStatistics{" +
                "bankId=" + bankId +
                ", mode=" + mode +
                ", currentOnline=" + currentOnline +
                ", maxOnline=" + maxOnline +
                ", minOnline=" + minOnline +
                ", date=" + new Date(date) +
                '}';
    }
}
