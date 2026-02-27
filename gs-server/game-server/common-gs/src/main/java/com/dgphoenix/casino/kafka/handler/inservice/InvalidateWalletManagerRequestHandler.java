package com.abs.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.dgphoenix.casino.kafka.dto.InvalidateWalletManagerRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class InvalidateWalletManagerRequestHandler implements KafkaInServiceAsyncRequestHandler<InvalidateWalletManagerRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public InvalidateWalletManagerRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(InvalidateWalletManagerRequest request) {
        serviceHandler.invalidateWalletManager(request.getBankId());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<InvalidateWalletManagerRequest> getRequestClass() {
        return InvalidateWalletManagerRequest.class;
    }

}
