package com.dgphoenix.casino.gs.managers.game.socket;

import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.socket.ISocketClient;

/**
 * Created
 * Date: 02.12.2008
 * Time: 18:26:44
 */
public interface IGameSocketObserver {
    boolean acceptConnection(ISocketClient client, SessionInfo sessionInfo);

    void connectionClosed(ISocketClient client, SessionInfo sessionInfo);
}
