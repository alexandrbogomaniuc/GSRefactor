package com.abs.casino.gs.managers.payment;

import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 15.11.14.
 */
public interface IStartGameProcessor {
    void process(GameSession gameSession, AccountInfo accountInfo, SessionInfo sessionInfo) throws CommonException;
}
