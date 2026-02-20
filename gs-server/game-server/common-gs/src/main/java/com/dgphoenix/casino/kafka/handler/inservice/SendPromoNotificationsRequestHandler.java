package com.dgphoenix.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.dgphoenix.casino.kafka.dto.SendPromoNotificationsRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class SendPromoNotificationsRequestHandler implements KafkaInServiceAsyncRequestHandler<SendPromoNotificationsRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public SendPromoNotificationsRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(SendPromoNotificationsRequest request) {
        serviceHandler.sendPromoNotifications(request.getSessionId(), request.getCampaignId(), request.getNotificationsTypes());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<SendPromoNotificationsRequest> getRequestClass() {
        return SendPromoNotificationsRequest.class;
    }

}
