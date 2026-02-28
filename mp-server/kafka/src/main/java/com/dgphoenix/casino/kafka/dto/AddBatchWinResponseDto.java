package com.abs.casino.kafka.dto;

import java.util.Map;

public class AddBatchWinResponseDto extends BasicKafkaResponse {
    private Map<Long, AddWinResultDto> winResults;

    public AddBatchWinResponseDto() {
        super();
    }

    public AddBatchWinResponseDto(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
    }

    public AddBatchWinResponseDto(Map<Long, AddWinResultDto> winResults,
            boolean success,
            int errorCode,
            String errorDetails) {
        super(success, errorCode, errorDetails);
        this.winResults = winResults;
    }

    public Map<Long, AddWinResultDto> getWinResults() {
        return winResults;
    }

    public void setWinResults(Map<Long, AddWinResultDto> winResults) {
        this.winResults = winResults;
    }

}
