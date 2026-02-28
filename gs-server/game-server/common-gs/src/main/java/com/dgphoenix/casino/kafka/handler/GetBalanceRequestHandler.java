package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.GetBalanceRequest;
import com.dgphoenix.casino.kafka.dto.LongResponseDto;

@Component
public class GetBalanceRequestHandler
        implements KafkaOuterRequestHandler<GetBalanceRequest, LongResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public GetBalanceRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public LongResponseDto handle(GetBalanceRequest request) {
        return new LongResponseDto(
                mqServiceHandler.getBalance(request.getSessionId(), request.getMode()));
    }

    @Override
    public Class<GetBalanceRequest> getRequestClass() {
        return GetBalanceRequest.class;
    }

}
