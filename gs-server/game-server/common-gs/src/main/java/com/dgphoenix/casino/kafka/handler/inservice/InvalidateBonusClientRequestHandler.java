package com.abs.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.gs.socket.InServiceServiceHandler;
import com.abs.casino.kafka.dto.InvalidateBonusClientRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.abs.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class InvalidateBonusClientRequestHandler implements KafkaInServiceAsyncRequestHandler<InvalidateBonusClientRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public InvalidateBonusClientRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(InvalidateBonusClientRequest request) {
        serviceHandler.invalidateBonusClient(request.getBankId());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<InvalidateBonusClientRequest> getRequestClass() {
        return InvalidateBonusClientRequest.class;
    }

}
