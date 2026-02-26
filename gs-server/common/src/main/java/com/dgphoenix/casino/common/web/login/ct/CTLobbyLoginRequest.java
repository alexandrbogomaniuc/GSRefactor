package com.abs.casino.common.web.login.ct;

import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.web.AbstractLobbyRequest;

/**
 * Created by : angel
 * Date: 11.02.2011
 * Time: 12:09:37
 */
public class CTLobbyLoginRequest extends AbstractLobbyRequest {
    String token;

    public CTLobbyLoginRequest(int bankId, short subCasinoId, String userHost, ClientType clientType, String token) {
        super(bankId, subCasinoId, userHost, clientType);
        this.token = token;
    }

    public CTLobbyLoginRequest(int bankId, short subCasinoId, String host, String token) {
        super(bankId, subCasinoId, host);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CTLobbyLoginRequest");
        sb.append("[bankId='").append(getBankId()).append('\'');
        sb.append(", subCasinoId='").append(getSubCasinoId()).append('\'');
        sb.append(", userHost='").append(getUserHost()).append('\'');
        sb.append(", clientType=").append(getClientType());
        sb.append(", token=").append(getToken());
        sb.append(super.toString());
        sb.append(']');
        return sb.toString();
    }
}
