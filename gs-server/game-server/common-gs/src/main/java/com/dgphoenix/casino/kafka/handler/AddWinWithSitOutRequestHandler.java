package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.AddWinResultDto;
import com.abs.casino.kafka.dto.AddWinWithSitOutRequest;

@Component
public class AddWinWithSitOutRequestHandler 
       implements KafkaOuterRequestHandler<AddWinWithSitOutRequest, AddWinResultDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public AddWinWithSitOutRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public AddWinResultDto handle(AddWinWithSitOutRequest request) {
        return mqServiceHandler.addWinWithSitOut(request.getSessionId(), request.getGameSessionId(), request.getCents(), request.getReturnedBet(), request.getRoundId(), request.getRoundId(), request.getRoomId(), request.getAccountId(), request.getRoundInfo(), request.getContributions(), request.isSitOut());
    }

    @Override
    public Class<AddWinWithSitOutRequest> getRequestClass() {
        return AddWinWithSitOutRequest.class;
    }

}
