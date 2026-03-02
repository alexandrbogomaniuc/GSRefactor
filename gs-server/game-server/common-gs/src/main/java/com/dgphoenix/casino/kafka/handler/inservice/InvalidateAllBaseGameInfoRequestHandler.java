package com.abs.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.gs.socket.InServiceServiceHandler;
import com.abs.casino.kafka.dto.InvalidateAllBaseGameInfoRequest;
import com.abs.casino.kafka.dto.VoidKafkaResponse;
import com.abs.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class InvalidateAllBaseGameInfoRequestHandler implements KafkaInServiceAsyncRequestHandler<InvalidateAllBaseGameInfoRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public InvalidateAllBaseGameInfoRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(InvalidateAllBaseGameInfoRequest request) {
        serviceHandler.invalidateAllBaseGameInfo(request.getBankId());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<InvalidateAllBaseGameInfoRequest> getRequestClass() {
        return InvalidateAllBaseGameInfoRequest.class;
    }

}
