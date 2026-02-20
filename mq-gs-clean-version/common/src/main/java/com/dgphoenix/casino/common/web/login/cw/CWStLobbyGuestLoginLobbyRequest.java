package com.dgphoenix.casino.common.web.login.cw;

import com.dgphoenix.casino.common.web.AbstractLobbyRequest;

/**
 * User: Grien
 * Date: 12.10.2011 18:42
 */
public class CWStLobbyGuestLoginLobbyRequest extends AbstractLobbyRequest {
    public CWStLobbyGuestLoginLobbyRequest(int bankId, short subCasinoId, String userHost) {
        super(bankId, subCasinoId, userHost);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CWStLobbyGuestLoginLobbyRequest");
        sb.append("[");
        sb.append(super.toString());
        sb.append(']');
        return sb.toString();
    }
}
