package com.dgphoenix.casino.kafka.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.BuyInRequest;
import com.dgphoenix.casino.kafka.dto.BuyInResultDto;

@Component
public class BuyInRequestHandler implements KafkaOuterRequestHandler<BuyInRequest, BuyInResultDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public BuyInRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public BuyInResultDto handle(BuyInRequest request) {
        return mqServiceHandler.buyIn3(request.getSessionId(), request.getCents(),
                request.getGameSessionId(), request.getRoomId(), request.getBetNumber(),
                request.getTournamentId(), request.getCurrentBalance(), request.getRoundId());
    }

    @Override
    public Class<BuyInRequest> getRequestClass() {
        return BuyInRequest.class;
    }

}
