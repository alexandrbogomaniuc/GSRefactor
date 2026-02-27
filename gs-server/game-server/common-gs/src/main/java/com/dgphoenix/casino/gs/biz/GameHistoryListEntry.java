package com.abs.casino.gs.biz;

import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 25.08.2009
 */
public class GameHistoryListEntry implements Serializable {
    private long roundId;
    private int gameStateId;
    private String data;
    private String servletData;
    private long bet;
    private long win;
    private long balance;
    private long time;
    private String archiveAdditionalData;

    public GameHistoryListEntry(PlayerBet playerBet) {
        this.roundId = playerBet.getRoundId();
        this.gameStateId = playerBet.getGameStateId();
        this.data = playerBet.getData();
        this.servletData = playerBet.getServletData();
        this.bet = playerBet.getBet();
        this.win = playerBet.getWin();
        this.balance = playerBet.getBalance();
        this.time = playerBet.getTime();
        this.archiveAdditionalData = Base64.encodeBase64String(playerBet.getArchiveAdditionalData());
    }

    public long getRoundId() {
        return roundId;
    }

    public int getGameStateId() {
        return gameStateId;
    }

    public String getData() {
        return data;
    }

    public String getServletData() {
        return servletData;
    }

    public long getBet() {
        return bet;
    }

    public long getWin() {
        return win;
    }

    public long getBalance() {
        return balance;
    }

    public long getTime() {
        return time;
    }

    public String getArchiveAdditionalData() {
        return archiveAdditionalData;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GameHistoryListEntry");
        sb.append("{roundId=").append(roundId);
        sb.append(", gameStateId=").append(gameStateId);
        sb.append(", data='").append(data).append('\'');
        sb.append(", servletData='").append(servletData).append('\'');
        sb.append(", bet=").append(bet);
        sb.append(", win=").append(win);
        sb.append(", balance=").append(balance);
        sb.append(", time=").append(time);
        sb.append(", archiveAdditionalData='").append(archiveAdditionalData).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
