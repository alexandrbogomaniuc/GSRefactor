package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.BGUpdateRoomResultDto;
import com.dgphoenix.casino.kafka.dto.UpdatePlayersStatusInPrivateRoomRequest;

@Component
public class UpdatePlayersStatusInPrivateRoomRequestHandler 
       implements KafkaOuterRequestHandler<UpdatePlayersStatusInPrivateRoomRequest, BGUpdateRoomResultDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public UpdatePlayersStatusInPrivateRoomRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public BGUpdateRoomResultDto handle(UpdatePlayersStatusInPrivateRoomRequest request) {
        return mqServiceHandler.updatePlayersStatusInPrivateRoom(request.getRequest());
    }

    @Override
    public Class<UpdatePlayersStatusInPrivateRoomRequest> getRequestClass() {
        return UpdatePlayersStatusInPrivateRoomRequest.class;
    }

}
