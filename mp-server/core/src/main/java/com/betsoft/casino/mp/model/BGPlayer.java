package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.privateroom.Status;

public class BGPlayer implements IBGPlayer {
    private String nickname;
    private long accountId;
    private String externalId;
    private Status status;

    public BGPlayer() {}

    public BGPlayer(String nickname, long accountId, String externalId, Status status) {
        this.nickname = nickname;
        this.accountId = accountId;
        this.externalId = externalId;
        this.status = status;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public String getExternalId() {
        return externalId;
    }

    public Status getStatus() {
        return status;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
