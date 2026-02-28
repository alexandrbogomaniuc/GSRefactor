package com.dgphoenix.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

public class StringResponseDto extends BasicKafkaResponse {
    private String value;

    public StringResponseDto() {}

    public StringResponseDto(boolean success, int statusCode, String reasonPhrases, String value) {
        super(success, statusCode, reasonPhrases);
        this.value = value;
    }

    public StringResponseDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }
    public StringResponseDto(String value) {
        super(true, 0, "");
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
