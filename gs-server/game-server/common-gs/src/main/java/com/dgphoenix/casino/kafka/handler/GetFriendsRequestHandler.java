package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.GetFriendsRequest;
import com.dgphoenix.casino.kafka.dto.GetFriendsResponseDto;

@Component
public class GetFriendsRequestHandler 
       implements KafkaOuterRequestHandler<GetFriendsRequest, GetFriendsResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public GetFriendsRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public GetFriendsResponseDto handle(GetFriendsRequest request) {
        return new GetFriendsResponseDto(mqServiceHandler.getFriends(request.getFriend()));
    }

    @Override
    public Class<GetFriendsRequest> getRequestClass() {
        return GetFriendsRequest.class;
    }

}
