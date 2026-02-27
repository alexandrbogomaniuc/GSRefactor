package com.abs.casino.gs.maintenance;

import java.io.Serializable;
import java.util.Date;

/**
 * User: flsh
 * Date: 22.10.2009
 */
public class LastGameSessionWrapper implements Serializable {
    private Date maxDate;
    private long accountId;
    private long gameId;

    public LastGameSessionWrapper() {
    }

    public LastGameSessionWrapper(Date maxDate, long accountId, long gameId) {
        this.maxDate = maxDate;
        this.accountId = accountId;
        this.gameId = gameId;
    }

    public Date getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("LastGameSessionWrapper");
        sb.append("[maxDate=").append(maxDate);
        sb.append(", accountId=").append(accountId);
        sb.append(", gameId=").append(gameId);
        sb.append(']');
        return sb.toString();
    }
}
