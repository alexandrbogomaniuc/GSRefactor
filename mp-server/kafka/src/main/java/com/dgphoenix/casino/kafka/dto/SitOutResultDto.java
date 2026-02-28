package com.abs.casino.kafka.dto;

public class SitOutResultDto extends BasicKafkaResponse {
    public SitOutResultDto() {
        super();
    }

    public SitOutResultDto(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
    }
}
