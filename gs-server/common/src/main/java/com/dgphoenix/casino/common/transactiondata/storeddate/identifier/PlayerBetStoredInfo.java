package com.dgphoenix.casino.common.transactiondata.storeddate.identifier;

import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;

/**
 * User: Grien
 * Date: 22.12.2014 12:32
 */
public class PlayerBetStoredInfo implements StoredItemInfo<PlayerBet> {
    private long gameSessionId;

    public PlayerBetStoredInfo(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PlayerBetStoredInfo");
        sb.append("[gameSessionId=").append(gameSessionId);
        sb.append(']');
        return sb.toString();
    }
}
