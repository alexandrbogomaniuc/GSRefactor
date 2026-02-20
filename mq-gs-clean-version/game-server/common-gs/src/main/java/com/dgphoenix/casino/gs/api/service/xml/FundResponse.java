package com.dgphoenix.casino.gs.api.service.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

@XStreamAlias("RESPONSE")
public class FundResponse implements Serializable {
    public static final String OK_RESULT = "OK";
    public static final String ERROR_RESULT = "ERROR";

    @XStreamAlias("RESULT")
    private String result;

    @XStreamAlias("BALANCE")
    private Long balance;

    @XStreamAlias("CODE")
    private String code;

    public FundResponse() {
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return OK_RESULT.equalsIgnoreCase(result);
    }

    @Override
    public String toString() {
        return "FundResponse{" +
                "result='" + result + '\'' +
                ", balance=" + balance +
                ", code='" + code + '\'' +
                '}';
    }
}
