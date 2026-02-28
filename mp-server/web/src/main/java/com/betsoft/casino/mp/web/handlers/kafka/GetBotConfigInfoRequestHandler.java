package com.betsoft.casino.mp.web.handlers.kafka;

import org.springframework.stereotype.Component;

import com.abs.casino.kafka.dto.BotConfigInfoDto;
import com.abs.casino.kafka.dto.GetBotConfigInfoRequest;
import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;


@Component
public class GetBotConfigInfoRequestHandler implements KafkaOuterRequestHandler<GetBotConfigInfoRequest, BotConfigInfoDto> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public GetBotConfigInfoRequestHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public BotConfigInfoDto handle(GetBotConfigInfoRequest request) {
        BotConfigInfoDto info = serviceHandler.getBotConfigInfo(request.getBotId());
        return info;
    }

    @Override
    public Class<GetBotConfigInfoRequest> getRequestClass() {
        return GetBotConfigInfoRequest.class;
    }
}
