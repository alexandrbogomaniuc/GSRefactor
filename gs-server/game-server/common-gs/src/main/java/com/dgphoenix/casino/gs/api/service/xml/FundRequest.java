package com.dgphoenix.casino.gs.api.service.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

@XStreamAlias("REQUEST")
public class FundRequest implements Serializable {
    @XStreamAlias("BANKID")
    private String bankId;

    @XStreamAlias("USERID")
    private String userId;

    @XStreamAlias("AMOUNT")
    private Long amount;

    @XStreamAlias("HASH")
    private String hash;

    public FundRequest() {
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

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "FundRequest{" +
                "bankId='" + bankId + '\'' +
                ", userId='" + userId + '\'' +
                ", amount='" + amount + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
