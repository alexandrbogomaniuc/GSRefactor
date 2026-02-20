package com.dgphoenix.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.dgphoenix.casino.kafka.dto.RefreshConfigRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class RefreshConfigRequestHandler implements KafkaInServiceAsyncRequestHandler<RefreshConfigRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public RefreshConfigRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(RefreshConfigRequest request) {
        serviceHandler.refreshConfig(request.getConfigName(), request.getObjectId());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<RefreshConfigRequest> getRequestClass() {
        return RefreshConfigRequest.class;
    }

}
