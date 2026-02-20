package com.dgphoenix.casino.kafka.dto;

public class BGPlayerDto {
    private String nickname;
    private long accountId;
    private String externalId;
    private BGStatus status;

    public BGPlayerDto() {}

    public BGPlayerDto(String nickname, long accountId, String externalId, BGStatus status) {
        this.nickname = nickname;
        this.accountId = accountId;
        this.externalId = externalId;
        this.status = status;
    }

    public String getNickname() {
        return nickname;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getExternalId() {
        return externalId;
    }

    public BGStatus getStatus() {
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

    public void setStatus(BGStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "PlayerDto{" +
                "nickname='" + nickname + '\'' +
                ", accountId=" + accountId +
                ", externalId='" + externalId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
