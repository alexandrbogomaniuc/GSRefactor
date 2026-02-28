package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.StartNewRoundRequest;
import com.dgphoenix.casino.kafka.dto.StartNewRoundResponseDto;

@Component
public class StartNewRoundRequestHandler
        implements KafkaOuterRequestHandler<StartNewRoundRequest, StartNewRoundResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public StartNewRoundRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public StartNewRoundResponseDto handle(StartNewRoundRequest request) {
        return mqServiceHandler.startNewRound2(request.getSessionId(), request.getAccountId(), request.getGameSessionId(),
                request.getRoomId(), request.getRoomRoundId(), request.getRoundStartDate(), request.isBattlegroundRoom(), request.getStakeOrBuyInAmount());
    }

    @Override
    public Class<StartNewRoundRequest> getRequestClass() {
        return StartNewRoundRequest.class;
    }

}
