package com.abs.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.abs.casino.kafka.dto.InvalidatePromoCampaignCacheRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class InvalidatePromoCampaignCacheRequestHandler implements KafkaInServiceAsyncRequestHandler<InvalidatePromoCampaignCacheRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public InvalidatePromoCampaignCacheRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(InvalidatePromoCampaignCacheRequest request) {
        serviceHandler.invalidatePromoCampaignCache(request.getPromoCampaignId());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<InvalidatePromoCampaignCacheRequest> getRequestClass() {
        return InvalidatePromoCampaignCacheRequest.class;
    }

}
