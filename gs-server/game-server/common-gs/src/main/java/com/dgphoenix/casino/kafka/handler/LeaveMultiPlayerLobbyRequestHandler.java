package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.LeaveMultiPlayerLobbyRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;

@Component
public class LeaveMultiPlayerLobbyRequestHandler 
       implements KafkaOuterRequestHandler<LeaveMultiPlayerLobbyRequest, VoidKafkaResponse> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public LeaveMultiPlayerLobbyRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public VoidKafkaResponse handle(LeaveMultiPlayerLobbyRequest request) {
        mqServiceHandler.leaveMultiPlayerLobby(request.getSessionId());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<LeaveMultiPlayerLobbyRequest> getRequestClass() {
        return LeaveMultiPlayerLobbyRequest.class;
    }

}
