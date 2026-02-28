package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.GetExternalAccountIdsRequest;
import com.dgphoenix.casino.kafka.dto.GetExternalAccountIdsResponseDto;

@Component
public class GetExternalAccountIdsRequestHandler implements KafkaOuterRequestHandler<GetExternalAccountIdsRequest, GetExternalAccountIdsResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public GetExternalAccountIdsRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public GetExternalAccountIdsResponseDto handle(GetExternalAccountIdsRequest request) {
        return new GetExternalAccountIdsResponseDto(mqServiceHandler.getExternalAccountIds(request.getAccountIds()));
    }

    @Override
    public Class<GetExternalAccountIdsRequest> getRequestClass() {
        return GetExternalAccountIdsRequest.class;
    }

}
