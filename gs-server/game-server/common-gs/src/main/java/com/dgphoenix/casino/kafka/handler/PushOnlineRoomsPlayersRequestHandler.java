package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.BooleanResponseDto;
import com.abs.casino.kafka.dto.PushOnlineRoomsPlayersRequest;

@Component
public class PushOnlineRoomsPlayersRequestHandler 
       implements KafkaOuterRequestHandler<PushOnlineRoomsPlayersRequest, BooleanResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public PushOnlineRoomsPlayersRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public BooleanResponseDto handle(PushOnlineRoomsPlayersRequest request) {
        boolean success = mqServiceHandler.pushOnlineRoomsPlayers(request.getRoomsPlayers());
        return new BooleanResponseDto(success);
    }

    @Override
    public Class<PushOnlineRoomsPlayersRequest> getRequestClass() {
        return PushOnlineRoomsPlayersRequest.class;
    }

}
