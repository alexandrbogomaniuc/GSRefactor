package com.dgphoenix.casino.bonus;


import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

@XStreamAlias("REQUEST")
public class BonusInfoRequest implements Serializable {
    @XStreamAlias("HASH")
    private String hash;

    @XStreamAlias("USERID")
    private String userId;

    @XStreamAlias("BANKID")
    private Long bankId;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GetBonusInfoRequest");
        sb.append("[hash='").append(hash).append('\'');
        sb.append(", userId=").append(userId);
        sb.append(", bankId=").append(bankId);
        sb.append(']');
        return sb.toString();
    }
}
