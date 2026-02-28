package com.abs.casino.kafka.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.kafka.dto.KafkaRequest;
import com.abs.casino.kafka.dto.KafkaResponse;

@Component("inServiceRequestHandlerFactory")
public class KafkaInServiceRequestHandlerFactory implements KafkaRequestHandlerFactory {
    private List<KafkaInServiceRequestHandler<? extends KafkaRequest, ? extends KafkaResponse>> handlers;

    @Autowired
    public KafkaInServiceRequestHandlerFactory(List<KafkaInServiceRequestHandler<? extends KafkaRequest, ? extends KafkaResponse>> handlers) {
        this.handlers = handlers;
    }

    @SuppressWarnings("unchecked")
    public <RQ extends KafkaRequest, RS extends KafkaResponse> KafkaInServiceRequestHandler<RQ, RS> getRequestHandler(RQ request) {
        return (KafkaInServiceRequestHandler<RQ, RS>) handlers.stream().filter(handler -> handler.getRequestClass().equals(request.getClass())).findFirst().orElseThrow(() -> new IllegalArgumentException("Handler for " + request.getClass().getName() + " not found in the system."));
    }
}
