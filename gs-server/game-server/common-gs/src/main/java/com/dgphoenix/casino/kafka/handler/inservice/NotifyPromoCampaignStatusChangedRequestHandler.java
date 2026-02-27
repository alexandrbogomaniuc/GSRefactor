package com.abs.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.dgphoenix.casino.kafka.dto.NotifyPromoCampaignStatusChangedRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class NotifyPromoCampaignStatusChangedRequestHandler implements KafkaInServiceAsyncRequestHandler<NotifyPromoCampaignStatusChangedRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public NotifyPromoCampaignStatusChangedRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(NotifyPromoCampaignStatusChangedRequest request) {
        serviceHandler.notifyPromoCampaignStatusChanged(request.getPromoCampaignId(), request.getOldStatus(), request.getNewStatus());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<NotifyPromoCampaignStatusChangedRequest> getRequestClass() {
        return NotifyPromoCampaignStatusChangedRequest.class;
    }

}
