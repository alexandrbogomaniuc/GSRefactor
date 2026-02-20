package com.dgphoenix.casino.kafka.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.BuyInRequest;
import com.dgphoenix.casino.kafka.dto.BuyInResultDto;
import com.dgphoenix.casino.kafka.dto.CheckBuyInRequest;

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
