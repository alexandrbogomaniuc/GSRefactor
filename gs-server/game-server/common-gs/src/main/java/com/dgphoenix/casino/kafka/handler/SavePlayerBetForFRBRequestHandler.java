package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.BooleanResponseDto;
import com.abs.casino.kafka.dto.SavePlayerBetForFRBRequest;

@Component
public class SavePlayerBetForFRBRequestHandler
        implements KafkaOuterRequestHandler<SavePlayerBetForFRBRequest, BooleanResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public SavePlayerBetForFRBRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public BooleanResponseDto handle(SavePlayerBetForFRBRequest request) {
        return new BooleanResponseDto(mqServiceHandler.savePlayerBetForFRB(request.getSessionId(),
                request.getGameSessionId(), request.getRoundId(), request.getAccountId(),
                request.getRoundInfo()));
    }

    @Override
    public Class<SavePlayerBetForFRBRequest> getRequestClass() {
        return SavePlayerBetForFRBRequest.class;
    }

}
