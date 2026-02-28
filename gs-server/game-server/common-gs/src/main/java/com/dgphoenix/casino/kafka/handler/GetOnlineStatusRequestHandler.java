package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.GetOnlineStatusRequest;
import com.abs.casino.kafka.dto.GetOnlineStatusResponseDto;

@Component
public class GetOnlineStatusRequestHandler implements KafkaOuterRequestHandler<GetOnlineStatusRequest, GetOnlineStatusResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public GetOnlineStatusRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public GetOnlineStatusResponseDto handle(GetOnlineStatusRequest request) {
        return new GetOnlineStatusResponseDto(mqServiceHandler.getOnlineStatus(request.getOnlinePlayers()));
    }

    @Override
    public Class<GetOnlineStatusRequest> getRequestClass() {
        return GetOnlineStatusRequest.class;
    }

}
