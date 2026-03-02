package com.abs.casino.gs.managers.payment.transfer.processor;

import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.ICloseGameProcessor;

public class DefaultCloseGameProcessor implements ICloseGameProcessor {

    @Override
    public void process(GameSession gameSession, AccountInfo accountInfo, ClientType clientType)
            throws CommonException {
        //ignore
    }

}
