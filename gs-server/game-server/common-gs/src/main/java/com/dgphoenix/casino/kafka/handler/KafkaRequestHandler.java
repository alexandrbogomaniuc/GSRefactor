package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.dto.KafkaRequest;
import com.abs.casino.kafka.dto.KafkaResponse;

public interface KafkaRequestHandler<RQ extends KafkaRequest, RS extends KafkaResponse> {
    RS handle(RQ request);
    Class<RQ> getRequestClass();
}
