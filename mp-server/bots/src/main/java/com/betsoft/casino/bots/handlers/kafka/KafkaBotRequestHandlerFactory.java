package com.betsoft.casino.bots.handlers.kafka;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;
import com.dgphoenix.casino.kafka.dto.KafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaRequestHandlerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("botRequestHandlerFactory")
public class KafkaBotRequestHandlerFactory implements KafkaRequestHandlerFactory {
    private List<KafkaBotRequestHandler<? extends KafkaRequest, ? extends KafkaResponse>> handlers;

    @Autowired
    public KafkaBotRequestHandlerFactory(List<KafkaBotRequestHandler<? extends KafkaRequest, ? extends KafkaResponse>> handlers) {
        this.handlers = handlers;
    }

    @SuppressWarnings("unchecked")
    public <RQ extends KafkaRequest, RS extends KafkaResponse> KafkaBotRequestHandler<RQ, RS> getRequestHandler(RQ request) {
        return (KafkaBotRequestHandler<RQ, RS>) handlers.stream().filter(handler -> handler.getRequestClass().equals(request.getClass())).findFirst().orElseThrow(() -> new IllegalArgumentException("Handler for " + request.getClass().getName() + " not found in the system."));
    }
}
