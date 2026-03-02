package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.ILobbySession;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.abs.casino.common.exception.CommonException;

import java.util.Collection;

/**
 * User: flsh
 * Date: 20.02.2020.
 */
public interface ILobbySessionService<LOBBY_SESSION extends ILobbySession> {
    ILobbySession add(LOBBY_SESSION session);

    Collection<LOBBY_SESSION> getByAccountId(long accountId);

    LOBBY_SESSION get(String sessionId);

    LOBBY_SESSION get(long accountId);

    void remove(String sessionId);

    boolean closeConnection(String sessionId);

    Runnable createRoundCompletedNotifyTask(String sid, long roomId, long accountId, long balance,
                                            long kills, long treasures, int rounds, long xp, long xpPrev,
                                            long xpNext, int level);

    void registerCloseLobbyConnectionListener(ILobbyConnectionClosedListener listener);

    void processCloseLobbyConnection(ILobbySocketClient client) throws CommonException;
}
