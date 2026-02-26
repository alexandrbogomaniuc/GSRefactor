package com.abs.casino.actions.api.response;

import com.thoughtworks.xstream.annotations.XStreamAliasType;

@XStreamAliasType("RESPONSE")
public abstract class Response {

    private String result;

    protected Response(String result) {
        this.result = result;
    }
}
