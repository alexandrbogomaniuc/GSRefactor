package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.BooleanResponseDto;
import com.abs.casino.kafka.dto.FinishGameSessionAndMakeSitOutRequest;

@Component
public class FinishGameSessionAndMakeSitOutRequestHandler
        implements KafkaOuterRequestHandler<FinishGameSessionAndMakeSitOutRequest, BooleanResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public FinishGameSessionAndMakeSitOutRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public BooleanResponseDto handle(FinishGameSessionAndMakeSitOutRequest request) {
        return new BooleanResponseDto(mqServiceHandler.finishGameSessionAndMakeSitOut(request.getSid(), request.getPrivateRoomId()));
    }

    @Override
    public Class<FinishGameSessionAndMakeSitOutRequest> getRequestClass() {
        return FinishGameSessionAndMakeSitOutRequest.class;
    }

}
