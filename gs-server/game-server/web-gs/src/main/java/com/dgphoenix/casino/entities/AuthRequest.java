package com.abs.casino.entities;

import com.abs.casino.common.cache.data.game.GameMode;
import com.abs.casino.common.cache.data.session.ClientType;

import java.util.HashMap;
import java.util.Map;

/**
 * User: isirbis
 * Date: 03.10.14
 */
public class AuthRequest {
    private String token;
    private GameMode gameMode;
    private String remoteHost;
    private ClientType clientType;

    private Map<String, String> properties = new HashMap<String, String>();

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void addProperty(String key, String value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
