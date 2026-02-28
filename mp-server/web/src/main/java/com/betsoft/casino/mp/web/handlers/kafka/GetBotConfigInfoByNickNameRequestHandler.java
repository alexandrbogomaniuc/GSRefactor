package com.betsoft.casino.mp.web.handlers.kafka;

import org.springframework.stereotype.Component;

import com.abs.casino.kafka.dto.BotConfigInfoDto;
import com.abs.casino.kafka.dto.GetBotConfigInfoByNickNameRequest;
import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;


@Component
public class GetBotConfigInfoByNickNameRequestHandler implements KafkaOuterRequestHandler<GetBotConfigInfoByNickNameRequest, BotConfigInfoDto> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public GetBotConfigInfoByNickNameRequestHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public BotConfigInfoDto handle(GetBotConfigInfoByNickNameRequest request) {
        BotConfigInfoDto info = serviceHandler.getBotConfigInfoByMqNickName(request.getNickname());
        return info;
    }

    @Override
    public Class<GetBotConfigInfoByNickNameRequest> getRequestClass() {
        return GetBotConfigInfoByNickNameRequest.class;
    }
}
