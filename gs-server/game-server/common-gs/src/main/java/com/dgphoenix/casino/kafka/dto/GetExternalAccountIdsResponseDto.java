package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

import java.util.Map;

public class GetExternalAccountIdsResponseDto extends BasicKafkaResponse {
    private Map<Long, String> externalAccountIds;

    public GetExternalAccountIdsResponseDto() {}

    public GetExternalAccountIdsResponseDto(Map<Long, String> externalAccountIds, boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
        this.externalAccountIds = externalAccountIds;
    }

    public GetExternalAccountIdsResponseDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public GetExternalAccountIdsResponseDto(Map<Long, String> externalAccountIds) {
        super(true, 0, "");
        this.externalAccountIds = externalAccountIds;
    }

    public Map<Long, String> getExternalAccountIds() {
        return externalAccountIds;
    }

    public void setExternalAccountIds(Map<Long, String> externalAccountIds) {
        this.externalAccountIds = externalAccountIds;
    }

}