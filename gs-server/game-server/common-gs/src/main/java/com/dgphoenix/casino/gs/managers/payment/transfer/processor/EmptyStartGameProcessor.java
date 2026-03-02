package com.abs.casino.gs.managers.payment.transfer.processor;

import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.payment.IStartGameProcessor;

public class EmptyStartGameProcessor implements IStartGameProcessor {
    @Override
    public void process(GameSession gameSession, AccountInfo accountInfo, SessionInfo sessionInfo)
            throws CommonException {
    }
}
