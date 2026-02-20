package com.dgphoenix.casino.gs.managers.payment.transfer.processor;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.managers.payment.IStartGameProcessor;

public class EmptyStartGameProcessor implements IStartGameProcessor {
    @Override
    public void process(GameSession gameSession, AccountInfo accountInfo, SessionInfo sessionInfo)
            throws CommonException {
    }
}
