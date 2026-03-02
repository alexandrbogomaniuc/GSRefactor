package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.BooleanResponseDto;
import com.abs.casino.kafka.dto.CloseGameSessionRequest;

@Component
public class CloseGameSessionRequestHandler
        implements KafkaOuterRequestHandler<CloseGameSessionRequest, BooleanResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public CloseGameSessionRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public BooleanResponseDto handle(CloseGameSessionRequest request) {
        return new BooleanResponseDto(mqServiceHandler.closeGameSession(request.getSessionId(),
                request.getAccountId(), request.getGameSessionId(), request.getBuyIn()));
    }

    @Override
    public Class<CloseGameSessionRequest> getRequestClass() {
        return CloseGameSessionRequest.class;
    }

}
