package com.abs.casino.gs.api.service.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

@XStreamAlias("REQUEST")
public class EnvironmentRequest implements Serializable {
    @XStreamAlias("HASH")
    private String hash;

    @XStreamAlias("BANKID")
    private String bankId;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("EnvironmentRequest");
        sb.append("[hash='").append(hash).append('\'');
        sb.append(", bankId=").append(bankId);
        sb.append(']');
        return sb.toString();
    }
}
