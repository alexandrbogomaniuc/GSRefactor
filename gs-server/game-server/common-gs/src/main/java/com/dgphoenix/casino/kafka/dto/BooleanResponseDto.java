package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

public class BooleanResponseDto extends BasicKafkaResponse {
    private boolean bool;

    public BooleanResponseDto() {}

    public BooleanResponseDto(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
        this.bool = false;
    }

    public BooleanResponseDto(boolean bool) {
        super(true, 0, "");
        this.bool = bool;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }
}
