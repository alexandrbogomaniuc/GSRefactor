package com.abs.casino.kafka.dto;

import java.util.List;

public class GetOnlineStatusResponseDto extends BasicKafkaResponse {
    private List<BGOnlinePlayerDto> players;

    public GetOnlineStatusResponseDto() {}

    public GetOnlineStatusResponseDto(List<BGOnlinePlayerDto> players, boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
        this.players = players;
    }

    public GetOnlineStatusResponseDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public GetOnlineStatusResponseDto(List<BGOnlinePlayerDto> players) {
        super(true, 0, "");
        this.players = players;
    }

    public List<BGOnlinePlayerDto> getPlayers() {
        return players;
    }

    public void setPlayers(List<BGOnlinePlayerDto> players) {
        this.players = players;
    }

}