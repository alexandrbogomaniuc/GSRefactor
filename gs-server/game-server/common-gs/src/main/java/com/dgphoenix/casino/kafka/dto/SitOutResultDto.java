package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

public class SitOutResultDto extends BasicKafkaResponse {
    public SitOutResultDto() {
        super();
    }

    public SitOutResultDto(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
    }
}
