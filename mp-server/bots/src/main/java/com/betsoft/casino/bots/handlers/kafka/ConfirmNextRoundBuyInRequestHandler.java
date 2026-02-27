package com.betsoft.casino.bots.handlers.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.betsoft.casino.bots.service.MQBBotServiceHandler;
import com.abs.casino.kafka.dto.bots.request.ConfirmNextRoundBuyInRequest;
import com.abs.casino.kafka.dto.bots.response.BotStatusResponse;


@Component
public class ConfirmNextRoundBuyInRequestHandler implements KafkaBotRequestHandler<ConfirmNextRoundBuyInRequest, BotStatusResponse> {
    @Autowired
    private MQBBotServiceHandler mqbBotServiceHandler;

    @Override
    public BotStatusResponse handle(ConfirmNextRoundBuyInRequest request) {
        return mqbBotServiceHandler.confirmNextRoundBuyIn(request.getBotId(), request.getSessionId(), request.getBotNickName(), request.getRoomId(), request.getRoundId());
    }

    @Override
    public Class<ConfirmNextRoundBuyInRequest> getRequestClass() {
        return ConfirmNextRoundBuyInRequest.class;
    }
}
