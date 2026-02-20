package com.dgphoenix.casino.kafka.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.StartNewRoundForManyPlayersRequest;
import com.dgphoenix.casino.kafka.dto.StartNewRoundForManyPlayersResponseDto;

@Component
public class StartNewRoundForManyPlayersRequestHandler implements
        KafkaOuterRequestHandler<StartNewRoundForManyPlayersRequest, StartNewRoundForManyPlayersResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public StartNewRoundForManyPlayersRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public StartNewRoundForManyPlayersResponseDto handle(StartNewRoundForManyPlayersRequest request) {
        return new StartNewRoundForManyPlayersResponseDto(
                mqServiceHandler.startNewRoundForManyPlayers(request.getRoundPlayers(),
                        request.getRoomId(), request.getRoomRoundId(), request.getRoundStartDate(),
                        request.isBattlegroundRoom(), request.getStakeOrBuyInAmount()));
    }

    @Override
    public Class<StartNewRoundForManyPlayersRequest> getRequestClass() {
        return StartNewRoundForManyPlayersRequest.class;
    }

}
