package com.dgphoenix.casino.kafka.dto.privateroom.request;

import com.dgphoenix.casino.kafka.dto.BGFriendDto;
import com.dgphoenix.casino.kafka.dto.KafkaRequest;

import java.util.List;

public class UpdateFriendsDto implements KafkaRequest {
    private String nickname;
    private String externalId;
    private List<BGFriendDto> friends;

    public UpdateFriendsDto(){}

    public UpdateFriendsDto(String nickname, String externalId, List<BGFriendDto> friends) {
        this.nickname = nickname;
        this.externalId = externalId;
        this.friends = friends;
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

    public List<BGFriendDto> getFriends() {
        return friends;
    }

    public void setFriends(List<BGFriendDto> friends) {
        this.friends = friends;
    }
}
