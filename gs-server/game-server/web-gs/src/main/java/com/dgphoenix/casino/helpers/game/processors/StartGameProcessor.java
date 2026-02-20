package com.dgphoenix.casino.helpers.game.processors;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.entities.game.requests.StartGameRequest;

import javax.servlet.http.HttpServletResponse;

/**
 * User: isirbis
 * Date: 09.10.14
 */
public class StartGameProcessor<R extends StartGameRequest> {
    public void additionalProcess(R startGameRequest, HttpServletResponse response, AccountInfo accountInfo,
                                  SessionInfo sessionInfo, IBaseGameInfo gameInfo, GameMode mode, Long gameSessionId)
            throws CommonException {
        //nop by default
    }

    public void afterGameStartedProcess(R startGameRequest, HttpServletResponse response, AccountInfo accountInfo,
                                        SessionInfo sessionInfo, IBaseGameInfo gameInfo, GameMode mode, Long gameSessionId)
            throws CommonException {
        //nop by default
    }
}
