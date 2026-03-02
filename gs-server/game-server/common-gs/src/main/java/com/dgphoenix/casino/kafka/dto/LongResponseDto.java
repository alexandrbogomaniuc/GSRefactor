package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.BasicKafkaResponse;

public class LongResponseDto extends BasicKafkaResponse {
    private long value;

    public LongResponseDto() {}

    public LongResponseDto(long value, boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
        this.value = value;
    }

    public LongResponseDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public LongResponseDto(long value) {
        super(true, 0, "");
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

}