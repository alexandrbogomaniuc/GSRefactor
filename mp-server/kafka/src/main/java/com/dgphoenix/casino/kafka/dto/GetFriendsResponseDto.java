package com.dgphoenix.casino.kafka.dto;

import java.util.List;

public class GetFriendsResponseDto extends BasicKafkaResponse {
    private List<BGFriendDto> friends;

    public GetFriendsResponseDto() {}

    public GetFriendsResponseDto(List<BGFriendDto> friends,
            boolean success,
            int errorCode,
            String errorDetails) {
        super(success, errorCode, errorDetails);
        this.friends = friends;
    }

    public GetFriendsResponseDto(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
    }

    public List<BGFriendDto> getFriends() {
        return friends;
    }

    public void setFriends(List<BGFriendDto> friends) {
        this.friends = friends;
    }

}
