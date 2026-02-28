package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

public class StartNewRoundResponseDto extends BasicKafkaResponse {
    private long playerRoundId;
    private long gameSessionId;

    public StartNewRoundResponseDto() {}

    public StartNewRoundResponseDto(long playerRoundId, long gameSessionId, boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
        this.playerRoundId = playerRoundId;
        this.gameSessionId = gameSessionId;
    }

    public StartNewRoundResponseDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public StartNewRoundResponseDto(long playerRoundId, long gameSessionId) {
        super(true, 0, "");
        this.playerRoundId = playerRoundId;
        this.gameSessionId = gameSessionId;
    }

    public long getPlayerRoundId() {
        return playerRoundId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public void setPlayerRoundId(long playerRoundId) {
        this.playerRoundId = playerRoundId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

}
