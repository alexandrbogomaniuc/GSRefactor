package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.AddWinRequestDto;
import com.abs.casino.kafka.dto.AddWinResultDto;

@Component
public class AddWinRequestHandler 
       implements KafkaOuterRequestHandler<AddWinRequestDto, AddWinResultDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public AddWinRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public AddWinResultDto handle(AddWinRequestDto request) {
        return mqServiceHandler.addWin(request.getSessionId(), request.getGameSessionId(), request.getCents(), request.getReturnedBet(), request.getGsRoundId(), request.getGsRoundId(), request.getGsRoomId(), request.getAccountId(), request.getRoundInfo(), request.getContributions());
    }

    @Override
    public Class<AddWinRequestDto> getRequestClass() {
        return AddWinRequestDto.class;
    }

}
