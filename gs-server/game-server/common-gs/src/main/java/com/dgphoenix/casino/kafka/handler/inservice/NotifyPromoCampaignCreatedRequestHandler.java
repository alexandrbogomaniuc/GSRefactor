package com.dgphoenix.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.dgphoenix.casino.kafka.dto.NotifyPromoCampaignCreatedRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class NotifyPromoCampaignCreatedRequestHandler implements KafkaInServiceAsyncRequestHandler<NotifyPromoCampaignCreatedRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public NotifyPromoCampaignCreatedRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(NotifyPromoCampaignCreatedRequest request) {
        serviceHandler.notifyPromoCampaignCreated(request.getPromoCampaignId());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<NotifyPromoCampaignCreatedRequest> getRequestClass() {
        return NotifyPromoCampaignCreatedRequest.class;
    }

}
