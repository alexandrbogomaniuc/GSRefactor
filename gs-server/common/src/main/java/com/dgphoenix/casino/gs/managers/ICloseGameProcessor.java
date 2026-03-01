package com.abs.casino.gs.managers;

import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 15.11.14.
 */
public interface ICloseGameProcessor {
    long TIME_OUT = 1000L * 60;

    void process(GameSession gameSession, AccountInfo accountInfo, ClientType clientType) throws CommonException;
}
