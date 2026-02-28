package com.betsoft.casino.mp.web.handlers.kafka;

import org.springframework.stereotype.Component;

import com.dgphoenix.casino.kafka.dto.BotConfigInfoDto;
import com.abs.casino.kafka.dto.GetBotConfigInfoByUserNameRequest;
import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;


@Component
public class GetBotConfigInfoByUserNameRequestHandler implements KafkaOuterRequestHandler<GetBotConfigInfoByUserNameRequest, BotConfigInfoDto> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public GetBotConfigInfoByUserNameRequestHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public BotConfigInfoDto handle(GetBotConfigInfoByUserNameRequest request) {
        BotConfigInfoDto info = serviceHandler.getBotConfigInfoByUserName(request.getUsername());
        return info;
    }

    @Override
    public Class<GetBotConfigInfoByUserNameRequest> getRequestClass() {
        return GetBotConfigInfoByUserNameRequest.class;
    }
}
