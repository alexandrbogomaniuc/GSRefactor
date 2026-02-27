package com.abs.casino.gs.api.service.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.Serializable;
import java.util.List;

@XStreamAlias("ACCOUNTS")
public class EnvironmentAccounts implements Serializable {

    @XStreamImplicit
    private List<EnvironmentAccount> accounts;

    public EnvironmentAccounts(List<EnvironmentAccount> accounts) {
        this.accounts = accounts;
    }

    public List<EnvironmentAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<EnvironmentAccount> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        return "EnvironmentAccounts{" +
                "accounts=" + accounts +
                '}';
    }
}
