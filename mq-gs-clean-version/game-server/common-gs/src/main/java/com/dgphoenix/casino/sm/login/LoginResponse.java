package com.dgphoenix.casino.sm.login;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;

/**
 * User: isirbis
 * Date: 07.10.14
 */
public class LoginResponse {
    private SessionInfo sessionInfo;
    private AccountInfo accountInfo;
    private Long notFinishedGameId;


    public LoginResponse(SessionInfo sessionInfo, AccountInfo accountInfo) {
        this.sessionInfo = sessionInfo;
        this.accountInfo = accountInfo;
    }

    public LoginResponse(SessionInfo sessionInfo, AccountInfo accountInfo, Long notFinishedGameId) {
        this.sessionInfo = sessionInfo;
        this.accountInfo = accountInfo;
        this.notFinishedGameId = notFinishedGameId;
    }

    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    public void setSessionInfo(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public Long getNotFinishedGameId() {
        return notFinishedGameId;
    }

    public void setNotFinishedGameId(Long notFinishedGameId) {
        this.notFinishedGameId = notFinishedGameId;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "notFinishedGameId=" + notFinishedGameId +
                ", sessionInfo=" + sessionInfo +
                ", accountInfo=" + accountInfo +
                '}';
    }
}
