package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.AddMQReservedNicknamesRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;

@Component
public class AddMQReservedNicknamesRequestHandler 
       implements KafkaOuterRequestHandler<AddMQReservedNicknamesRequest, VoidKafkaResponse> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public AddMQReservedNicknamesRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public VoidKafkaResponse handle(AddMQReservedNicknamesRequest request) {
        mqServiceHandler.addMQReservedNicknames(request.getRegion(), request.getOwner(), request.getNicknames());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<AddMQReservedNicknamesRequest> getRequestClass() {
        return AddMQReservedNicknamesRequest.class;
    }

}
