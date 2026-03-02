package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.SitOutRequest;
import com.abs.casino.kafka.dto.SitOutResultDto;

@Component
public class SitOutRequestHandler 
       implements KafkaOuterRequestHandler<SitOutRequest, SitOutResultDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public SitOutRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public SitOutResultDto handle(SitOutRequest request) {
        return mqServiceHandler.sitOut(request.getSessionId(), request.getGameSessionId(), request.getCents(), request.getReturnedBet(), request.getRoundId(), request.getRoomId(), request.getAccountId(), request.getRoundInfo(), request.getContributions());
    }

    @Override
    public Class<SitOutRequest> getRequestClass() {
        return SitOutRequest.class;
    }

}
