package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.StartNewRoundResponseDto;

import com.abs.casino.kafka.dto.BasicKafkaResponse;

import java.util.Map;

public class StartNewRoundForManyPlayersResponseDto extends BasicKafkaResponse {
    private Map<Long, StartNewRoundResponseDto> results;

    public StartNewRoundForManyPlayersResponseDto() {}

    public StartNewRoundForManyPlayersResponseDto( Map<Long, StartNewRoundResponseDto> results, boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
        this.results = results;
    }

    public StartNewRoundForManyPlayersResponseDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public StartNewRoundForManyPlayersResponseDto(Map<Long, StartNewRoundResponseDto> results) {
        super(true, 0, "");
        this.results = results;
    }

    public Map<Long, StartNewRoundResponseDto> getResults() {
        return results;
    }

    public void setResults(Map<Long, StartNewRoundResponseDto> results) {
        this.results = results;
    }

}