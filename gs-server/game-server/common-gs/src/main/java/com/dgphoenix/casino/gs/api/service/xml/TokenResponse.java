package com.abs.casino.gs.api.service.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

@XStreamAlias("RESPONSE")
public class TokenResponse implements Serializable {
    public static final String OK_RESULT = "OK";
    public static final String ERROR_RESULT = "ERROR";

    @XStreamAlias("RESULT")
    private String result;

    @XStreamAlias("TOKEN")
    private String token;

    @XStreamAlias("LONGTERM")
    private Boolean longTerm;

    @XStreamAlias("CODE")
    private String code;

    public TokenResponse() {
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getLongTerm() {
        return longTerm;
    }

    public void setLongTerm(Boolean longTerm) {
        this.longTerm = longTerm;
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
        return "TokenResponse{" +
                "result='" + result + '\'' +
                ", token='" + token + '\'' +
                ", longTerm='" + longTerm + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
