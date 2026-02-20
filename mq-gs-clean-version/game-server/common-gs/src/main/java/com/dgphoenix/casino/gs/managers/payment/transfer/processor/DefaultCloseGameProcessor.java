package com.dgphoenix.casino.gs.managers.payment.transfer.processor;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.managers.ICloseGameProcessor;

public class DefaultCloseGameProcessor implements ICloseGameProcessor {

    @Override
    public void process(GameSession gameSession, AccountInfo accountInfo, ClientType clientType)
            throws CommonException {
        //ignore
    }

}
