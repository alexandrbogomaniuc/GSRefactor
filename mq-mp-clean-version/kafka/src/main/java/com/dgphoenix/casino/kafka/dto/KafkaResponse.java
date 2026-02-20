package com.dgphoenix.casino.kafka.dto;

public interface KafkaResponse extends KafkaMessage {
    boolean isSuccess();
    int getStatusCode();
    String getReasonPhrases();
}
