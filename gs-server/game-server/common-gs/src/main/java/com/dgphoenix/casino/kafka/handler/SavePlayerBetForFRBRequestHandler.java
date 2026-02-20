package com.dgphoenix.casino.kafka.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.BooleanResponseDto;
import com.dgphoenix.casino.kafka.dto.SavePlayerBetForFRBRequest;

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
