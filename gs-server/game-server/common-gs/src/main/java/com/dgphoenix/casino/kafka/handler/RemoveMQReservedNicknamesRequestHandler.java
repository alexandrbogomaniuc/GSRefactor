package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.RemoveMQReservedNicknamesRequest;
import com.abs.casino.kafka.dto.VoidKafkaResponse;

@Component
public class RemoveMQReservedNicknamesRequestHandler 
       implements KafkaOuterRequestHandler<RemoveMQReservedNicknamesRequest, VoidKafkaResponse> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public RemoveMQReservedNicknamesRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public VoidKafkaResponse handle(RemoveMQReservedNicknamesRequest request) {
        mqServiceHandler.removeMQReservedNicknames(request.getRegion(), request.getOwner(), request.getNicknames());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<RemoveMQReservedNicknamesRequest> getRequestClass() {
        return RemoveMQReservedNicknamesRequest.class;
    }

}
