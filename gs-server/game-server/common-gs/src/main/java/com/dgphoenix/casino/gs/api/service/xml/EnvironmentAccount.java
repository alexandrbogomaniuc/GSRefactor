package com.dgphoenix.casino.gs.api.service.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

@XStreamAlias("ACCOUNT")
public class EnvironmentAccount implements Serializable {

    @XStreamAlias("USERID")
    private String userId;

    @XStreamAlias("LOGIN")
    private String login;

    @XStreamAlias("PASSWORD")
    private String password;

    @XStreamAlias("CURRENCY")
    private String currency;

    @XStreamAlias("BALANCE")
    private Long balance;

    @XStreamAlias("INUSE")
    private Boolean inUse;

    public EnvironmentAccount() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Boolean getInUse() {
        return inUse;
    }

    public void setInUse(Boolean inUse) {
        this.inUse = inUse;
    }

    @Override
    public String toString() {
        return "EnvironmentAccount{" +
                "userId='" + userId + '\'' +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", currency='" + currency + '\'' +
                ", balance=" + balance +
                ", inUse='" + inUse + '\'' +
                '}';
    }
}
