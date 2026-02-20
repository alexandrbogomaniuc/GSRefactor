package com.dgphoenix.casino.gs.api.service.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

@XStreamAlias("REQUEST")
public class TokenRequest implements Serializable {
    @XStreamAlias("BANKID")
    private String bankId;

    @XStreamAlias("USERID")
    private String userId;

    @XStreamAlias("HASH")
    private String hash;

    public TokenRequest() {
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "TokenRequest{" +
                "bankId='" + bankId + '\'' +
                ", userId='" + userId + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
