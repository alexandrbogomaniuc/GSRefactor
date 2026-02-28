package com.abs.casino.common.web;

import com.dgphoenix.casino.common.cache.data.session.ClientType;

/**
 * User: plastical
 * Date: 23.04.2010
 */
public abstract class AbstractLobbyRequest {
    private int bankId;
    private short subCasinoId;
    private String userHost;
    private ClientType clientType;

    protected AbstractLobbyRequest(int bankId, short subCasinoId, String userHost, ClientType clientType) {
        this.bankId = bankId;
        this.subCasinoId = subCasinoId;
        this.userHost = userHost;
        this.clientType = clientType;
    }

    protected AbstractLobbyRequest(int bankId, short subCasinoId, String host) {
        this.bankId = bankId;
        this.subCasinoId = subCasinoId;
        this.userHost = host;
        this.clientType = ClientType.FLASH;
    }

    public int getBankId() {
        return bankId;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public short getSubCasinoId() {
        return subCasinoId;
    }

    public void setSubCasinoId(short subCasinoId) {
        this.subCasinoId = subCasinoId;
    }

    public String getUserHost() {
        return userHost;
    }

    public void setUserHost(String userHost) {
        this.userHost = userHost;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(", bankId=").append(bankId);
        sb.append(", subCasinoId=").append(subCasinoId);
        sb.append(", userHost='").append(userHost).append('\'');
        sb.append(", clientType=").append(clientType);
        return sb.toString();
    }
}
