package com.abs.casino.controller.mqb.response;

public class GetPrivateRoomIdOkResult extends Result{

    String privateRoomId;

    public GetPrivateRoomIdOkResult(String result, String privateRoomId) {
        super(result);
        this.privateRoomId = privateRoomId;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }
}
