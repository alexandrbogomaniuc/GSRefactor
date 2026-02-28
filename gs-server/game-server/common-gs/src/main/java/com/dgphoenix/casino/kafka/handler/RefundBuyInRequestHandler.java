package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.RefundBuyInRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;

@Component
public class RefundBuyInRequestHandler
        implements KafkaOuterRequestHandler<RefundBuyInRequest, VoidKafkaResponse> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public RefundBuyInRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public VoidKafkaResponse handle(RefundBuyInRequest request) {
        return mqServiceHandler.refundBuyIn(request.getSessionId(), request.getCents(),
                request.getAccountId(), request.getGameSessionId(), request.getRoomId(),
                request.getBetNumber());
    }

    @Override
    public Class<RefundBuyInRequest> getRequestClass() {
        return RefundBuyInRequest.class;
    }

}
