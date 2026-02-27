package com.betsoft.casino.bots.handlers.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.betsoft.casino.bots.service.MQBBotServiceHandler;
import com.abs.casino.kafka.dto.bots.request.BotLogInRequest;
import com.abs.casino.kafka.dto.bots.response.BotLogInResultDto;


@Component
public class BotLogInHandler implements KafkaBotRequestHandler<BotLogInRequest, BotLogInResultDto> {
    @Autowired
    private MQBBotServiceHandler mqbBotServiceHandler;

    @Override
    public BotLogInResultDto handle(BotLogInRequest request) {
        return mqbBotServiceHandler.logIn(request.getBotId(), request.getUserName(), request.getPassword(), request.getBankId(), request.getGameId(), request.getBuyIn(), request.getBotNickname(),
                request.getRoomId(), request.getLang(), request.getGameServerId(), request.getEnterLobbyWsUrl(), request.getOpenRoomWSUrl(), request.getExpiresAt(), request.getShootsRate(), request.getBulletsRate());
    }

    @Override
    public Class<BotLogInRequest> getRequestClass() {
        return BotLogInRequest.class;
    }
}
