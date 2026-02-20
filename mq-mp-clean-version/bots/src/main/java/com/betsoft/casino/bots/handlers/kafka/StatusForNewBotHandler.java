package com.betsoft.casino.bots.handlers.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.betsoft.casino.bots.service.MQBBotServiceHandler;
import com.dgphoenix.casino.kafka.dto.bots.request.BotStatusRequest;
import com.dgphoenix.casino.kafka.dto.bots.response.BotStatusResponse;


@Component
public class StatusForNewBotHandler implements KafkaBotRequestHandler<BotStatusRequest, BotStatusResponse> {
    @Autowired
    private MQBBotServiceHandler mqbBotServiceHandler;

    @Override
    public BotStatusResponse handle(BotStatusRequest request) {
        return mqbBotServiceHandler.getStatusForNewBot(request.getUserName(), request.getPassword(), request.getBotNickName(), request.getBankId(), request.getGameId());
    }

    @Override
    public Class<BotStatusRequest> getRequestClass() {
        return BotStatusRequest.class;
    }
}
