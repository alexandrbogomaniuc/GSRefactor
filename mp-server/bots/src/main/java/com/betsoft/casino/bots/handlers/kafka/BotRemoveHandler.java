package com.betsoft.casino.bots.handlers.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.betsoft.casino.bots.service.MQBBotServiceHandler;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.abs.casino.kafka.dto.bots.request.RemoveBotRequest;


@Component
public class BotRemoveHandler implements KafkaBotRequestHandler<RemoveBotRequest, VoidKafkaResponse> {
    @Autowired
    private MQBBotServiceHandler mqbBotServiceHandler;

    @Override
    public VoidKafkaResponse handle(RemoveBotRequest request) {
        mqbBotServiceHandler.removeBot(request.getBotId(), request.getBotNickName(), request.getRoomId());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<RemoveBotRequest> getRequestClass() {
        return RemoveBotRequest.class;
    }
}
