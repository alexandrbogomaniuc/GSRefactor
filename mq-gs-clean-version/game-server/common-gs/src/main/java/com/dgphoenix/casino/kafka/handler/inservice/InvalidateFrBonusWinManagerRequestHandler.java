package com.dgphoenix.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.dgphoenix.casino.kafka.dto.InvalidateFrBonusWinManagerRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class InvalidateFrBonusWinManagerRequestHandler implements KafkaInServiceAsyncRequestHandler<InvalidateFrBonusWinManagerRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public InvalidateFrBonusWinManagerRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(InvalidateFrBonusWinManagerRequest request) {
        serviceHandler.invalidateFrBonusWinManager(request.getBankId());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<InvalidateFrBonusWinManagerRequest> getRequestClass() {
        return InvalidateFrBonusWinManagerRequest.class;
    }

}
