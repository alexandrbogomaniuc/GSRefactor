package com.betsoft.casino.bots.handlers.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.betsoft.casino.bots.service.MQBBotServiceHandler;
import com.dgphoenix.casino.kafka.dto.StringResponseDto;
import com.abs.casino.kafka.dto.bots.request.GetDetailBotInfoRequest;

@Component
public class GetDetailBotInfoRequestHandler implements KafkaBotRequestHandler<GetDetailBotInfoRequest, StringResponseDto> {
    @Autowired
    private MQBBotServiceHandler mqbBotServiceHandler;

    @Override
    public StringResponseDto handle(GetDetailBotInfoRequest request) {
        String botStr = mqbBotServiceHandler.getDetailBotInfo(request.getBotId(), request.getBotNickName());
        return new StringResponseDto(botStr);
    }

    @Override
    public Class<GetDetailBotInfoRequest> getRequestClass() {
        return GetDetailBotInfoRequest.class;
    }
}
