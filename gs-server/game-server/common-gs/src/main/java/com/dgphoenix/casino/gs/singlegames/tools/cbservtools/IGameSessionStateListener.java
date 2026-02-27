package com.abs.casino.gs.singlegames.tools.cbservtools;

import com.dgphoenix.casino.common.cache.data.session.GameSession;

/**
 * User: Grien
 * Date: 10.06.2014 12:24
 */
public interface IGameSessionStateListener {
    public static enum State {
        START,
        CLOSE
    }

    void listen(State state, GameSession gameSession, int bankId);
}
