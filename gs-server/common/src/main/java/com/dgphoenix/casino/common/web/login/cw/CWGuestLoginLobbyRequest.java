package com.abs.casino.common.web.login.cw;

import com.dgphoenix.casino.common.web.AbstractLobbyRequest;

/**
 * User: plastical
 * Date: 23.04.2010
 */
public class CWGuestLoginLobbyRequest extends AbstractLobbyRequest {
    private long gameId;

    public CWGuestLoginLobbyRequest(int bankId, short subCasinoId, String userHost, long gameId) {
        super(bankId, subCasinoId, userHost);
        this.gameId = gameId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CWGuestLoginLobbyRequest");
        sb.append("[gameId=").append(gameId);
        sb.append(super.toString());
        sb.append(']');
        return sb.toString();
    }
}
