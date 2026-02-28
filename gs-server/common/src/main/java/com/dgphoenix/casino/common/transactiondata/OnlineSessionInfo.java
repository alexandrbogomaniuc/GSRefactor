package com.abs.casino.common.transactiondata;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;

/**
 * User: flsh
 * Date: 11.04.15.
 */
public class OnlineSessionInfo {
    private AccountInfo account;
    private SessionInfo sessionInfo;
    private GameSession gameSession;

    public OnlineSessionInfo() {
    }

    public OnlineSessionInfo(AccountInfo account, SessionInfo sessionInfo, GameSession gameSession) {
        this.account = account;
        this.sessionInfo = sessionInfo;
        this.gameSession = gameSession;
    }

    public AccountInfo getAccount() {
        return account;
    }

    public void setAccount(AccountInfo account) {
        this.account = account;
    }

    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    public void setSessionInfo(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public GameSession getGameSession() {
        return gameSession;
    }

    public void setGameSession(GameSession gameSession) {
        this.gameSession = gameSession;
    }

    @Override
    public String toString() {
        return "OnlineSessionInfo[" +
                "account=" + account +
                ", sessionInfo=" + sessionInfo +
                ", gameSession=" + gameSession +
                ']';
    }
}
