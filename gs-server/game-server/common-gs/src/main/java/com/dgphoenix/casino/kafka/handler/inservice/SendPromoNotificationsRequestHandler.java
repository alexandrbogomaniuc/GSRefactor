package com.abs.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.gs.socket.InServiceServiceHandler;
import com.abs.casino.kafka.dto.SendPromoNotificationsRequest;
import com.abs.casino.kafka.dto.VoidKafkaResponse;
import com.abs.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

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
