package com.abs.casino.gs.managers.game.core;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.dblink.IDBLink;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface IGameProcessor {
    long getGameId();

    IDBLink registerAccount(AccountInfo accountInfo, GameMode mode, String lastHand, Long gameSessionId, Long bonusId,
                            SessionInfo sessionInfo, IBaseGameInfo gameInfo, String lang, boolean restart)
            throws CommonException;

    void closeGameSession(GameSession gameSession, boolean limitsChanged, SessionInfo sessionInfo)
            throws CommonException;

    IDBLink getDBLink(long accountId) throws CommonException;

    Map<String, String> processCommand(String cmd, Long accountId, HttpServletRequest request,
                                       HttpServletResponse response)
            throws IOException, ServletException, CommonException;
}
