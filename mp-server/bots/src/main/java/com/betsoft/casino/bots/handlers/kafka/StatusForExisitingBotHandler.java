package com.betsoft.casino.bots.handlers.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.betsoft.casino.bots.service.MQBBotServiceHandler;
import com.dgphoenix.casino.kafka.dto.bots.request.BotGetStatusRequest;
import com.dgphoenix.casino.kafka.dto.bots.response.BotStatusResponse;

@Component
public class StatusForExisitingBotHandler implements KafkaBotRequestHandler<BotGetStatusRequest, BotStatusResponse> {
    @Autowired
    private MQBBotServiceHandler mqbBotServiceHandler;

    @Override
    public BotStatusResponse handle(BotGetStatusRequest request) {
        return mqbBotServiceHandler.getStatus(request.getBotId(), request.getSessionId(), request.getBotNickName(), request.getRoomId());
    }

    @Override
    public Class<BotGetStatusRequest> getRequestClass() {
        return BotGetStatusRequest.class;
    }
}
