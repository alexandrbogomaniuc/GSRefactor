package com.betsoft.casino.mp.model.friends;

import java.util.Objects;

public class Friend {
    private String nickname;
    private String externalId;
    private Status status;
    private long updateTime;

    public Friend() {}

    public Friend(String nickname, String externalId, Status status) {
        this(nickname, externalId, status, System.currentTimeMillis());
    }

    public Friend(String nickname, String externalId, Status status, long updateTime) {
        this.nickname = nickname;
        this.externalId = externalId;
        this.status = status;
        this.updateTime = updateTime;
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

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updateTime = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friend)) return false;
        Friend that = (Friend) o;
        return Objects.equals(nickname, that.nickname)
                && Objects.equals(externalId, that.externalId)
                && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname, externalId, status);
    }

    @Override
    public String toString() {
        return "Friend{" +
                "nickname='" + nickname + '\'' +
                ", externalId='" + externalId + '\'' +
                ", status=" + status +
                ", updateTime=" + updateTime +
                '}';
    }
}
