package com.dgphoenix.casino.actions.enter;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 10/6/11
 */
public class AccountInfoAndSessionInfoPair implements Serializable {
    private final AccountInfo account;
    private SessionInfo sessionInfo;

    public AccountInfoAndSessionInfoPair(AccountInfo account) {
        this.account = account;
    }

    public AccountInfoAndSessionInfoPair(AccountInfo account, SessionInfo sessionInfo) {
        this.account = account;
        this.sessionInfo = sessionInfo;
    }

    public AccountInfo getAccount() {
        return account;
    }

    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    public void setSessionInfo(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AccountInfoAndSessionInfoPair");
        sb.append("[account=").append(account);
        sb.append(", sessionInfo=").append(sessionInfo);
        sb.append(']');
        return sb.toString();
    }
}
