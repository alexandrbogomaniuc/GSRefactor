package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;
import com.dgphoenix.casino.kafka.dto.KafkaResponse;

public interface KafkaInServiceRequestHandler<RQ extends KafkaRequest, RS extends KafkaResponse> extends KafkaRequestHandler<RQ, RS>{
}
