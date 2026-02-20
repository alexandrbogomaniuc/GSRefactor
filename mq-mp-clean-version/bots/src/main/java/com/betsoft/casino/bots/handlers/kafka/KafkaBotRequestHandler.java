package com.betsoft.casino.bots.handlers.kafka;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;
import com.dgphoenix.casino.kafka.dto.KafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaRequestHandler;

public interface KafkaBotRequestHandler<RQ extends KafkaRequest, RS extends KafkaResponse> extends KafkaRequestHandler<RQ, RS> {
}
