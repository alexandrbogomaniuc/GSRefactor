package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;

public interface KafkaInServiceAsyncRequestHandler<RQ extends KafkaRequest> extends KafkaInServiceRequestHandler<RQ, VoidKafkaResponse>{
}
