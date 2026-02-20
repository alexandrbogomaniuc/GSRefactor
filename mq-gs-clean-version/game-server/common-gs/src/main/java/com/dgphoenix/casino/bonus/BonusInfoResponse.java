package com.dgphoenix.casino.bonus;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@XStreamAlias("RESPONSE")
public class BonusInfoResponse implements Serializable {
    public static final String OK_RESULT = "OK";
    public static final String ERROR_RESULT = "ERROR";

    @XStreamAlias("RESULT")
    private String result;

    @XStreamAlias("DESCRIPTION")
    private String description;

    @XStreamAlias("CODE")
    private String code;

    @XStreamImplicit
    private List<Bonus> bonuses;

    public BonusInfoResponse() {
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Bonus> getBonuses() {
        return bonuses;
    }

    public void setBonuses(List<Bonus> bonuses) {
        this.bonuses = bonuses;
    }

    public boolean isSuccess() {
        return OK_RESULT.equalsIgnoreCase(result);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BonusInfoResponse");
        sb.append("[result='").append(result).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(", bonuses=").append(bonuses == null ? "null" : Arrays.asList(bonuses));
        sb.append(']');
        return sb.toString();
    }
}
