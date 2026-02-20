package com.betsoft.casino.mp.web.handlers.kafka;

import java.util.List;

import org.springframework.stereotype.Component;

import com.dgphoenix.casino.kafka.dto.BotConfigInfoDto;
import com.dgphoenix.casino.kafka.dto.BotConfigInfosResponse;
import com.dgphoenix.casino.kafka.dto.RemoveBotConfigInfoRequest;
import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;


@Component
public class RemoveBotConfigInfoRequestHandler implements KafkaOuterRequestHandler<RemoveBotConfigInfoRequest, BotConfigInfosResponse> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public RemoveBotConfigInfoRequestHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public BotConfigInfosResponse handle(RemoveBotConfigInfoRequest request) {
        List<BotConfigInfoDto> infos = serviceHandler.removeBotConfigInfo(request.getBotIds());
        return new BotConfigInfosResponse(infos);
    }

    @Override
    public Class<RemoveBotConfigInfoRequest> getRequestClass() {
        return RemoveBotConfigInfoRequest.class;
    }
}
