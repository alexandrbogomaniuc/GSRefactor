package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.model.IPlayerStats;
import com.betsoft.casino.mp.model.PlayerStats;

public class CollectResult {
    private IPlayerStats playerStats;
    private boolean isNewLevel;
    private int newLevel;
    private int errorCode;
    private long xp;
    private long xpPrev;
    private long xpNext;


    public CollectResult(IPlayerStats playerStats, boolean isNewLevel, int newLevel, int errorCode) {
        this.playerStats = playerStats;
        this.isNewLevel = isNewLevel;
        this.newLevel = newLevel;
        this.errorCode = errorCode;
    }

    public CollectResult() {
    }

    public int getNewLevel() {
        return newLevel;
    }

    public void setNewLevel(int newLevel) {
        this.newLevel = newLevel;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public IPlayerStats getPlayerStats() {
        return playerStats;
    }

    public void setPlayerStats(IPlayerStats playerStats) {
        this.playerStats = playerStats;
    }

    public boolean isNewLevel() {
        return isNewLevel;
    }

    public void setNewLevel(boolean newLevel) {
        isNewLevel = newLevel;
    }


    public long getXp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = xp;
    }

    public long getXpPrev() {
        return xpPrev;
    }

    public void setXpPrev(long xpPrev) {
        this.xpPrev = xpPrev;
    }

    public long getXpNext() {
        return xpNext;
    }

    public void setXpNext(long xpNext) {
        this.xpNext = xpNext;
    }

    @Override
    public String toString() {
        return "CollectResult[" +
                "playerStats=" + playerStats +
                ", isNewLevel=" + isNewLevel +
                ", newLevel=" + newLevel +
                ", errorCode=" + errorCode +
                ", xp=" + xp +
                ", xpPrev=" + xpPrev +
                ", xpNext=" + xpNext +
                ']';
    }
}
