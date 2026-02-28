package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.SitOutCashBonusSessionRequest;
import com.abs.casino.kafka.dto.SitOutCashBonusSessionResultDto;

@Component
public class SitOutCashBonusSessionRequestHandler implements
        KafkaOuterRequestHandler<SitOutCashBonusSessionRequest, SitOutCashBonusSessionResultDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public SitOutCashBonusSessionRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public SitOutCashBonusSessionResultDto handle(SitOutCashBonusSessionRequest request) {
        return mqServiceHandler.sitOutCashBonusSession(request.getAccountId(),
                request.getSessionId(), request.getGameSessionId(), request.getBonusId(),
                request.getBalance(), request.getBetSum(), request.getData(),
                request.getRoundInfo(), request.getRoundId());
    }

    @Override
    public Class<SitOutCashBonusSessionRequest> getRequestClass() {
        return SitOutCashBonusSessionRequest.class;
    }

}
