package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.dto.KafkaRequest;
import com.abs.casino.kafka.dto.VoidKafkaResponse;

public interface KafkaInServiceAsyncRequestHandler<RQ extends KafkaRequest> extends KafkaInServiceRequestHandler<RQ, VoidKafkaResponse>{
}
