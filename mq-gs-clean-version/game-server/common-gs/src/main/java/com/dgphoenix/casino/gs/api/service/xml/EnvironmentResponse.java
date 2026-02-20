package com.dgphoenix.casino.gs.api.service.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

@XStreamAlias("RESPONSE")
public class EnvironmentResponse implements Serializable {
    public static final String OK_RESULT = "OK";
    public static final String ERROR_RESULT = "ERROR";

    @XStreamAlias("RESULT")
    private String result;

    @XStreamAlias("LOBBY_URL")
    private String lobbyUrl;

    @XStreamAlias("BSG_GAMES_URL")
    private String gamesUrl;

    @XStreamAlias("USE_BSG_PROXY")
    private String userProxy;

    @XStreamAlias("CODE")
    private String code;

    @XStreamAlias("ACCOUNTS")
    private EnvironmentAccounts accounts;

    public EnvironmentResponse() {
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getLobbyUrl() {
        return lobbyUrl;
    }

    public void setLobbyUrl(String lobbyUrl) {
        this.lobbyUrl = lobbyUrl;
    }

    public String getGamesUrl() {
        return gamesUrl;
    }

    public void setGamesUrl(String gamesUrl) {
        this.gamesUrl = gamesUrl;
    }

    public String getUserProxy() {
        return userProxy;
    }

    public void setUserProxy(String userProxy) {
        this.userProxy = userProxy;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public EnvironmentAccounts getAccounts() {
        return accounts;
    }

    public void setAccounts(EnvironmentAccounts accounts) {
        this.accounts = accounts;
    }

    public boolean isSuccess() {
        return OK_RESULT.equalsIgnoreCase(result);
    }

    @Override
    public String toString() {
        return "EnvironmentResponse{" +
                "result='" + result + '\'' +
                ", lobbyUrl='" + lobbyUrl + '\'' +
                ", gamesUrl='" + gamesUrl + '\'' +
                ", userProxy='" + userProxy + '\'' +
                ", code='" + code + '\'' +
                ", accounts=" + accounts +
                '}';
    }
}
