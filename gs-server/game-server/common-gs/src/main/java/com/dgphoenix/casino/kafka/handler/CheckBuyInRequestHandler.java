package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.BuyInRequest;
import com.abs.casino.kafka.dto.BuyInResultDto;
import com.abs.casino.kafka.dto.CheckBuyInRequest;

@Component
public class CheckBuyInRequestHandler
        implements KafkaOuterRequestHandler<CheckBuyInRequest, BuyInResultDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public CheckBuyInRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public BuyInResultDto handle(CheckBuyInRequest request) {
        return mqServiceHandler.checkBuyIn(request.getSessionId(), request.getCents(),
                request.getAccountId(), request.getGameSessionId(), request.getRoomId(),
                request.getBetNumber());
    }

    @Override
    public Class<CheckBuyInRequest> getRequestClass() {
        return CheckBuyInRequest.class;
    }

}
