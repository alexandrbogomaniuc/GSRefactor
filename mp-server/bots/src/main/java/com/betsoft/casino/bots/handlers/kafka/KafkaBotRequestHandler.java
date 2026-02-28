package com.betsoft.casino.bots.handlers.kafka;

import com.abs.casino.kafka.dto.KafkaRequest;
import com.abs.casino.kafka.dto.KafkaResponse;
import com.abs.casino.kafka.handler.KafkaRequestHandler;

public interface KafkaBotRequestHandler<RQ extends KafkaRequest, RS extends KafkaResponse> extends KafkaRequestHandler<RQ, RS> {
}
