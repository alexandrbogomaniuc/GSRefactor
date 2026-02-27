package com.abs.casino.sm.login;

import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * User: isirbis
 * Date: 07.10.14
 */
public class LoginRequest {
    /**
     * For CW: UserId
     * For Red7BS: sessionId
     * For FS: sessionId
     */
    protected String token;
    protected String externalSessionId;

    protected Short subCasinoId;
    protected Integer bankId;

    protected String remoteHost;
    protected ClientType clientType;

    protected Map<String, String> properties = new HashMap<>();
    protected boolean reusePlayerSession = false;

    protected String privateRoomId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExternalSessionId() {
        return externalSessionId;
    }

    public void setExternalSessionId(String externalSessionId) {
        this.externalSessionId = externalSessionId;
    }

    public Short getSubCasinoId() {
        return subCasinoId;
    }

    public void setSubCasinoId(Short subCasinoId) {
        this.subCasinoId = subCasinoId;
    }

    public Integer getBankId() {
        return bankId;
    }

    public void setBankId(Integer bankId) {
        this.bankId = bankId;
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

    public boolean isReusePlayerSession() {
        return reusePlayerSession;
    }

    public void setReusePlayerSession(boolean reusePlayerSession) {
        this.reusePlayerSession = reusePlayerSession;
    }


    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    @Override
    public String toString() {
        return "LoginRequest[" +
                "token='" + token + '\'' +
                ", externalSessionId='" + externalSessionId + '\'' +
                ", subCasinoId=" + subCasinoId +
                ", bankId=" + bankId +
                ", remoteHost='" + remoteHost + '\'' +
                ", clientType=" + clientType +
                ", reusePlayerSession=" + isReusePlayerSession() +
                ", properties=" + CollectionUtils.mapToString(properties) +
                ", privateRoomId=" + privateRoomId +
                ']';
    }
}
