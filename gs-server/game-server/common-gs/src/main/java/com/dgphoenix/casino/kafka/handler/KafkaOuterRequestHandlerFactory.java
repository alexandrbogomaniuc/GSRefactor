package com.abs.casino.kafka.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;
import com.dgphoenix.casino.kafka.dto.KafkaResponse;

@Component("outerRequestHandlerFactory")
public class KafkaOuterRequestHandlerFactory implements KafkaRequestHandlerFactory {
    private List<KafkaOuterRequestHandler<? extends KafkaRequest, ? extends KafkaResponse>> handlers;

    @Autowired
    public KafkaOuterRequestHandlerFactory(List<KafkaOuterRequestHandler<? extends KafkaRequest, ? extends KafkaResponse>> handlers) {
        this.handlers = handlers;
    }

    @SuppressWarnings("unchecked")
    public <RQ extends KafkaRequest, RS extends KafkaResponse> KafkaOuterRequestHandler<RQ, RS> getRequestHandler(RQ request) {
        return (KafkaOuterRequestHandler<RQ, RS>) handlers.stream().filter(handler -> handler.getRequestClass().equals(request.getClass())).findFirst().orElseThrow(() -> new IllegalArgumentException("Handler for " + request.getClass().getName() + " not found in the system."));
    }
}
