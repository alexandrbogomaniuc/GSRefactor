package com.dgphoenix.casino.kafka.handler;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;
import com.dgphoenix.casino.kafka.dto.KafkaResponse;

public interface KafkaRequestHandler<RQ extends KafkaRequest, RS extends KafkaResponse> {
    RS handle(RQ request);
    Class<RQ> getRequestClass();
}
