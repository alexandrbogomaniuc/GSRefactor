package com.dgphoenix.casino.common.web.login.cw;

import com.dgphoenix.casino.common.web.BasicGameServerResponse;

/**
 * User: plastical
 * Date: 23.04.2010
 */
public class CWGuestLoginGameServerResponse extends BasicGameServerResponse {
    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CWGuestLoginGameServerResponse");
        sb.append("{sessionId='").append(sessionId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
