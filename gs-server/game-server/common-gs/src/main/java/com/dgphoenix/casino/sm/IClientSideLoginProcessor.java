package com.abs.casino.sm;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;

/**
 * User: flsh
 * Date: Jun 22, 2010
 */
public interface IClientSideLoginProcessor {
    void process(AccountInfo accountInfo, SessionInfo sessionInfo);
}
