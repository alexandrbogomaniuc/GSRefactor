package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.BooleanResponseDto;
import com.dgphoenix.casino.kafka.dto.InvitePlayersToPrivateRoomRequest;

@Component
public class InvitePlayersToPrivateRoomRequestHandler 
       implements KafkaOuterRequestHandler<InvitePlayersToPrivateRoomRequest, BooleanResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public InvitePlayersToPrivateRoomRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public BooleanResponseDto handle(InvitePlayersToPrivateRoomRequest request) {
        return new BooleanResponseDto(mqServiceHandler.invitePlayersToPrivateRoom(request.getPlayers(), request.getPrivateRoomId()));
    }

    @Override
    public Class<InvitePlayersToPrivateRoomRequest> getRequestClass() {
        return InvitePlayersToPrivateRoomRequest.class;
    }

}
