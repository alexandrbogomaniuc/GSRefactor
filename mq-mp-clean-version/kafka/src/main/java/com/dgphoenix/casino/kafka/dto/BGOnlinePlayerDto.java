package com.dgphoenix.casino.kafka.dto;

public class BGOnlinePlayerDto {
    private String nickname;
    private String externalId;
    private BGOStatus status;

    public BGOnlinePlayerDto() {}

    public BGOnlinePlayerDto(String nickname, String externalId, BGOStatus status) {
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

    public BGOStatus getStatus() {
        return status;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setStatus(BGOStatus status) {
        this.status = status;
    }
}
