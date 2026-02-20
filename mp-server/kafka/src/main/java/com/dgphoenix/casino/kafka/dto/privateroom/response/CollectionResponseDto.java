package com.dgphoenix.casino.kafka.dto.privateroom.response;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

import java.util.Set;

public class CollectionResponseDto extends BasicKafkaResponse {
    private Set<Long> value;

    public CollectionResponseDto() {}

    public CollectionResponseDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public CollectionResponseDto(boolean success, int statusCode, String reasonPhrases, Set<Long> value) {
        super(success, statusCode, reasonPhrases);
        this.value = value;
    }

    public Set<Long> getValue() {
        return value;
    }

    public void setValue(Set<Long> value) {
        this.value = value;
    }
}

