package com.dgphoenix.casino.web.bonus.transport;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * User: flsh
 * Date: 06.02.13
 */
@XStreamAlias("response")
public class Response {
    @XStreamAlias("RESULT")
    private String result = null;
    @XStreamAlias("CODE")
    private String code = null;

    public Response() {
    }

    public Response(String result, String code) {
        this.result = result;
        this.code = code;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Response");
        sb.append("{result='").append(result).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
