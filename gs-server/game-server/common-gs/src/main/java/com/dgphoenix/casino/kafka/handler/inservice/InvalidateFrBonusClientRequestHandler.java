package com.abs.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.abs.casino.kafka.dto.InvalidateFrBonusClientRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class InvalidateFrBonusClientRequestHandler implements KafkaInServiceAsyncRequestHandler<InvalidateFrBonusClientRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public InvalidateFrBonusClientRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(InvalidateFrBonusClientRequest request) {
        serviceHandler.invalidateFrBonusClient(request.getBankId());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<InvalidateFrBonusClientRequest> getRequestClass() {
        return InvalidateFrBonusClientRequest.class;
    }

}
