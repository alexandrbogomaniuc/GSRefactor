package com.betsoft.casino.mp.web.handlers.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.kafka.dto.BooleanResponseDto;
import com.abs.casino.kafka.dto.IsBotServiceEnabledRequest;
import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;

@Component
public class IsBotServiceEnabledRequestHandler 
       implements KafkaOuterRequestHandler<IsBotServiceEnabledRequest, BooleanResponseDto> {

    private final KafkaMultiPlayerResponseService serviceHandler;

    @Autowired
    public IsBotServiceEnabledRequestHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public BooleanResponseDto handle(IsBotServiceEnabledRequest request) {
        return new BooleanResponseDto(serviceHandler.isBotServiceEnabled());
    }

    @Override
    public Class<IsBotServiceEnabledRequest> getRequestClass() {
        return IsBotServiceEnabledRequest.class;
    }

}
