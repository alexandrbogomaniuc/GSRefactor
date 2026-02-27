package com.betsoft.casino.bots.handlers.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.betsoft.casino.bots.service.MQBBotServiceHandler;
import com.betsoft.casino.mp.model.bots.dto.BotsMap;
import com.abs.casino.kafka.dto.bots.request.GetBotsMapRequest;
import com.abs.casino.kafka.dto.bots.response.BotsMapResponseDto;

@Component
public class GetBotsMapRequestHandler implements KafkaBotRequestHandler<GetBotsMapRequest, BotsMapResponseDto> {
    @Autowired
    private MQBBotServiceHandler mqbBotServiceHandler;

    @Override
    public BotsMapResponseDto handle(GetBotsMapRequest request) {
        BotsMap botsMap = mqbBotServiceHandler.getBotsMap();
        return new BotsMapResponseDto(botsMap);
    }

    @Override
    public Class<GetBotsMapRequest> getRequestClass() {
        return GetBotsMapRequest.class;
    }
}
