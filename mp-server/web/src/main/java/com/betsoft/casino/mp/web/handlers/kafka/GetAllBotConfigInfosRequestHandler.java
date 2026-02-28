package com.betsoft.casino.mp.web.handlers.kafka;

import java.util.List;

import org.springframework.stereotype.Component;

import com.abs.casino.kafka.dto.BotConfigInfoDto;
import com.abs.casino.kafka.dto.BotConfigInfosResponse;
import com.abs.casino.kafka.dto.GetAllBotConfigInfosRequest;
import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;


@Component
public class GetAllBotConfigInfosRequestHandler implements KafkaOuterRequestHandler<GetAllBotConfigInfosRequest, BotConfigInfosResponse> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public GetAllBotConfigInfosRequestHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public BotConfigInfosResponse handle(GetAllBotConfigInfosRequest request) {
        List<BotConfigInfoDto> infos = serviceHandler.getAllBotConfigInfos();
        return new BotConfigInfosResponse(infos);
    }

    @Override
    public Class<GetAllBotConfigInfosRequest> getRequestClass() {
        return GetAllBotConfigInfosRequest.class;
    }
}
