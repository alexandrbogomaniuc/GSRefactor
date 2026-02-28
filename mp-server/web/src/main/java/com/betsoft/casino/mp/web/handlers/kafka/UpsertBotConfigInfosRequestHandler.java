package com.betsoft.casino.mp.web.handlers.kafka;

import java.util.List;

import org.springframework.stereotype.Component;

import com.dgphoenix.casino.kafka.dto.BotConfigInfoDto;
import com.dgphoenix.casino.kafka.dto.BotConfigInfosResponse;
import com.abs.casino.kafka.dto.UpsertBotConfigInfoRequest;
import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;


@Component
public class UpsertBotConfigInfosRequestHandler implements KafkaOuterRequestHandler<UpsertBotConfigInfoRequest, BotConfigInfosResponse> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public UpsertBotConfigInfosRequestHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public BotConfigInfosResponse handle(UpsertBotConfigInfoRequest request) {
        List<BotConfigInfoDto> infos = serviceHandler.upsertBotConfigInfo(request.getBotConfigInfos());
        return new BotConfigInfosResponse(infos);
    }

    @Override
    public Class<UpsertBotConfigInfoRequest> getRequestClass() {
        return UpsertBotConfigInfoRequest.class;
    }
}
