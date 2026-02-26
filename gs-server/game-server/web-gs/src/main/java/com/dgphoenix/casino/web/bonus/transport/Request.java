package com.abs.casino.web.bonus.transport;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * User: flsh
 * Date: 06.02.13
 */
@XStreamAlias("request")
public class Request {
    @XStreamAlias("CMD")
    private String cmd;

    @XStreamAlias("USERID")
    private String userId;

    @XStreamAlias("BANKID")
    private String bankId;

    public Request() {
    }

    public Request(String cmd, String userId, String bankId) {
        this.cmd = cmd;
        this.userId = userId;
        this.bankId = bankId;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
        sb.append("Request");
        sb.append("[cmd='").append(cmd).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", bankId='").append(bankId).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
