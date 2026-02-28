package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.dto.KafkaRequest;
import com.abs.casino.kafka.dto.KafkaResponse;

public interface KafkaOuterRequestHandler<RQ extends KafkaRequest, RS extends KafkaResponse> extends KafkaRequestHandler<RQ, RS> {
}
