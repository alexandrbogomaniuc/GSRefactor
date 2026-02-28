package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.GetPaymentOperationStatus2Request;
import com.abs.casino.kafka.dto.StringResponseDto;

@Component
public class GetPaymentOperationStatus2RequestHandler 
       implements KafkaOuterRequestHandler<GetPaymentOperationStatus2Request, StringResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public GetPaymentOperationStatus2RequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public StringResponseDto handle(GetPaymentOperationStatus2Request request) {
        return new StringResponseDto(mqServiceHandler.getPaymentOperationStatus(request.getAccountId(), request.getRoomId(), request.getRoundId(), request.getSessionId(), request.getGameSessionId(), request.getGameId(), request.getBankId(), request.getIsBet(), request.getBetNumber()));
    }

    @Override
    public Class<GetPaymentOperationStatus2Request> getRequestClass() {
        return GetPaymentOperationStatus2Request.class;
    }

}
