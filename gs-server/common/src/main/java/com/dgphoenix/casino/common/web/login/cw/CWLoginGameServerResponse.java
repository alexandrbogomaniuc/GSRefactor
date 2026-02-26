package com.abs.casino.common.web.login.cw;

import com.dgphoenix.casino.common.web.BasicGameServerResponse;

public class CWLoginGameServerResponse extends BasicGameServerResponse {
    private String sessionId;
    private String currency;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CWLoginGameServerResponse");
        sb.append("{sessionId='").append(sessionId).append('\'');
        sb.append("{currency='").append(currency).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
