package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.BGFriendDto;
import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class GetFriendsRequest implements KafkaRequest {
    private BGFriendDto friend;

    public GetFriendsRequest() {}

    public GetFriendsRequest(BGFriendDto friend) {
        this.friend = friend;
    }

    public BGFriendDto getFriend() {
        return friend;
    }

    public void setFriend(BGFriendDto friend) {
        this.friend = friend;
    }
}
