package com.dgphoenix.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.dgphoenix.casino.kafka.dto.NotifySessionClosedRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class NotifySessionClosedRequestHandler implements KafkaInServiceAsyncRequestHandler<NotifySessionClosedRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public NotifySessionClosedRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(NotifySessionClosedRequest request) {
        serviceHandler.notifySessionClosed(request.getSessionId());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<NotifySessionClosedRequest> getRequestClass() {
        return NotifySessionClosedRequest.class;
    }

}
