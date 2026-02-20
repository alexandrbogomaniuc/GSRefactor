package com.dgphoenix.casino.gs.managers.payment;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 15.11.14.
 */
public interface IStartGameProcessor {
    void process(GameSession gameSession, AccountInfo accountInfo, SessionInfo sessionInfo) throws CommonException;
}
