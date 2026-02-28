package com.betsoft.casino.mp.web.handlers.kafka;

import org.springframework.stereotype.Component;

import com.abs.casino.kafka.dto.EnableBotServiceRequest;
import com.abs.casino.kafka.dto.VoidKafkaResponse;
import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;


@Component
public class EnableBotServiceHandler implements KafkaOuterRequestHandler<EnableBotServiceRequest, VoidKafkaResponse> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public EnableBotServiceHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(EnableBotServiceRequest request) {
        serviceHandler.enableBotService(request.isEnabled());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<EnableBotServiceRequest> getRequestClass() {
        return EnableBotServiceRequest.class;
    }
}
