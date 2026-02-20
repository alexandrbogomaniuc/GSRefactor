package com.dgphoenix.casino.common.cache.data.payment;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.data.game.GameSessionInfo;

import java.util.List;

/**
 * User: flsh
 * Date: 3/15/11
 */
public class GameSessionInfoContainer implements IDistributedCacheEntry {
    private List<GameSessionInfo> sessions;

    public GameSessionInfoContainer(List<GameSessionInfo> sessions) {
        this.sessions = sessions;
    }

    public GameSessionInfoContainer() {
    }

    public List<GameSessionInfo> getSessions() {
        return sessions;
    }

    public void setSessions(List<GameSessionInfo> sessions) {
        this.sessions = sessions;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GameSessionInfoContainer");
        sb.append("[sessions=").append(sessions);
        sb.append(']');
        return sb.toString();
    }
}
