package com.dgphoenix.casino.gs;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 20.03.13
 */
public interface ISendBetInterceptor {
    void processBet(Long roundId, GameSession gameSession, AccountInfo accountInfo, Logger log, long bet, long win)
            throws CommonException;

    void processRoundFinished(Long roundId, GameSession gameSession, AccountInfo accountInfo, Logger log);
}
