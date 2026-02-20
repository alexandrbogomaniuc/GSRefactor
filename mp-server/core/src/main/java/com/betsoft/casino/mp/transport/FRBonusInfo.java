package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IFRBonusInfo;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 03.08.18.
 */
public class FRBonusInfo implements IFRBonusInfo, Serializable {
    private long id;
    private long awardDate;
    private long expirationDate;
    private int totalShots;
    private int currentShots;
    private long winSum;
    private long stake;

    public FRBonusInfo(long id, long awardDate, long expirationDate, int totalShots, int currentShots, long winSum,
                       long stake) {
        this.id = id;
        this.awardDate = awardDate;
        this.expirationDate = expirationDate;
        this.totalShots = totalShots;
        this.currentShots = currentShots;
        this.winSum = winSum;
        this.stake = stake;
    }

    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getAwardDate() {
        return awardDate;
    }

    @Override
    public void setAwardDate(long awardDate) {
        this.awardDate = awardDate;
    }

    @Override
    public long getExpirationDate() {
        return expirationDate;
    }

    @Override
    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public int getTotalShots() {
        return totalShots;
    }

    @Override
    public void setTotalShots(int totalShots) {
        this.totalShots = totalShots;
    }

    @Override
    public int getCurrentShots() {
        return currentShots;
    }

    @Override
    public void setCurrentShots(int currentShots) {
        this.currentShots = currentShots;
    }

    @Override
    public long getWinSum() {
        return winSum;
    }

    @Override
    public void setWinSum(long winSum) {
        this.winSum = winSum;
    }

    @Override
    public long getStake() {
        return stake;
    }

    @Override
    public void setStake(long stake) {
        this.stake = stake;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FRBonusInfo [");
        sb.append("id=").append(id);
        sb.append(", awardDate=").append(awardDate);
        sb.append(", expirationDate=").append(expirationDate);
        sb.append(", totalShots=").append(totalShots);
        sb.append(", currentShots=").append(currentShots);
        sb.append(", winSum=").append(winSum);
        sb.append(", stake=").append(stake);
        sb.append(']');
        return sb.toString();
    }
}
