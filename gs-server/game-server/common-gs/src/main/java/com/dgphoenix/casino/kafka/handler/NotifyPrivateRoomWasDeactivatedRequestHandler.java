package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.NotifyPrivateRoomWasDeactivatedRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;

@Component
public class NotifyPrivateRoomWasDeactivatedRequestHandler 
       implements KafkaOuterRequestHandler<NotifyPrivateRoomWasDeactivatedRequest, VoidKafkaResponse> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public NotifyPrivateRoomWasDeactivatedRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public VoidKafkaResponse handle(NotifyPrivateRoomWasDeactivatedRequest request) {
        mqServiceHandler.notifyPrivateRoomWasDeactivated(request.getPrivateRoomId(), request.getReason(), request.getBankId());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<NotifyPrivateRoomWasDeactivatedRequest> getRequestClass() {
        return NotifyPrivateRoomWasDeactivatedRequest.class;
    }

}
