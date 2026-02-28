package com.abs.casino.kafka.dto;

public class BGFriendDto {
    private String nickname;
    private String externalId;
    private BGFStatus status;

    public BGFriendDto() {}

    public BGFriendDto(String nickname, String externalId, BGFStatus status) {
        this.nickname = nickname;
        this.externalId = externalId;
        this.status = status;
    }

    public String getNickname() {
        return nickname;
    }

    public String getExternalId() {
        return externalId;
    }

    public BGFStatus getStatus() {
        return status;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setStatus(BGFStatus status) {
        this.status = status;
    }
}
