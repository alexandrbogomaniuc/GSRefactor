package com.betsoft.casino.mp.model.privateroom;

public class Player {
    private long accountId;
    private String nickname;
    private String externalId;

    private Status status;

    private long updateTime;

    public Player() {
    }

    public Player(String nickname, long accountId, String externalId, Status status) {
        this(nickname, accountId, externalId, status, System.currentTimeMillis());
    }

    public Player(String nickname, long accountId, String externalId, Status status, long updateTime) {
        this.accountId = accountId;
        this.nickname = nickname;
        this.externalId = externalId;
        this.status = status;
        this.updateTime = updateTime;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
        this.updateTime = System.currentTimeMillis();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        this.updateTime = System.currentTimeMillis();
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
        this.updateTime = System.currentTimeMillis();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updateTime = System.currentTimeMillis();
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Player{" +
                "accountId=" + accountId +
                ", nickname='" + nickname + '\'' +
                ", externalId='" + externalId + '\'' +
                ", status=" + status +
                ", updateTime=" + updateTime +
                '}';
    }
}
