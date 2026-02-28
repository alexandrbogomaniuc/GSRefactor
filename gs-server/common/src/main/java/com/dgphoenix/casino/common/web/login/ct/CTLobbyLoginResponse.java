package com.abs.casino.common.web.login.ct;

import com.abs.casino.common.web.BasicGameServerResponse;

/**
 * Created by : angel
 * Date: 11.02.2011
 * Time: 12:16:35
 */
public class CTLobbyLoginResponse extends BasicGameServerResponse {
    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "CTLobbyLoginResponse{" +
                "status='" + getStatus() + '\'' +
                ",description='" + getDescription() + '\'' +
                ",bundleMapping='" + getBundleMapping() + '\'' +
                ",sessionId='" + sessionId + '\'' +
                '}';
    }
}
