package com.betsoft.casino.bots.handlers.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.betsoft.casino.bots.service.MQBBotServiceHandler;
import com.abs.casino.kafka.dto.bots.request.BotSitOutRequest;
import com.abs.casino.kafka.dto.bots.response.BotLogOutResultDto;


@Component
public class BotLogOutHandler implements KafkaBotRequestHandler<BotSitOutRequest, BotLogOutResultDto> {
    @Autowired
    private MQBBotServiceHandler mqbBotServiceHandler;

    @Override
    public BotLogOutResultDto handle(BotSitOutRequest request) {
        return mqbBotServiceHandler.logOut(request.getBotId(), request.getSessionId(), request.getBotNickName(), request.getRoomId());
    }

    @Override
    public Class<BotSitOutRequest> getRequestClass() {
        return BotSitOutRequest.class;
    }
}
