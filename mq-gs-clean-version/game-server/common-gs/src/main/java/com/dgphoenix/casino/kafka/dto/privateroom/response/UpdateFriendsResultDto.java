package com.dgphoenix.casino.kafka.dto.privateroom.response;

import java.util.List;

import com.dgphoenix.casino.kafka.dto.BGFriendDto;
import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

public class UpdateFriendsResultDto extends BasicKafkaResponse {
    private String nickname;
    private String externalId;
    private List<BGFriendDto> players;

    public UpdateFriendsResultDto(){}

    public UpdateFriendsResultDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public UpdateFriendsResultDto(String nickname, String externalId, List<BGFriendDto> players) {
        this.nickname = nickname;
        this.externalId = externalId;
        this.players = players;
    }

    public UpdateFriendsResultDto(boolean success, int statusCode, String reasonPhrases, String nickname, String externalId, List<BGFriendDto> players) {
        super(success, statusCode, reasonPhrases);
        this.nickname = nickname;
        this.externalId = externalId;
        this.players = players;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public List<BGFriendDto> getPlayers() {
        return players;
    }

    public void setPlayers(List<BGFriendDto> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return "UpdateFriendsResultDto{" +
                "nickname='" + nickname + '\'' +
                ", externalId='" + externalId + '\'' +
                ", players=" + players +
                '}';
    }
}
