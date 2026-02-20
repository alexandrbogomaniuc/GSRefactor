package com.dgphoenix.casino.sm;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: plastical
 * Date: 25.02.2010
 */
public interface IPlayerSessionManager {

    default SessionInfo login(AccountInfo accountInfo, String externalSessionId, String userHost, ClientType clientType,
                              boolean forceReuseGameSession)
            throws CommonException {
        return login(accountInfo, externalSessionId, userHost, clientType);
    }

    SessionInfo login(AccountInfo accountInfo, String externalSessionId, String userHost, ClientType clientType)
            throws CommonException;

    void logout(String sessionId) throws CommonException;

    void logout(AccountInfo accountInfo, String logoutReason, SessionInfo sessionInfo) throws CommonException;

    void refreshBankproperies();

    String getAlertsEmailAddress();

    public boolean isSendLoginErrorsToEmail();

    boolean isReusePlayerSession(long bankId);

}
