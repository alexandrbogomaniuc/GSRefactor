package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.MQDataDto;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;

@Component
public class StoreMQDataRequestHandler 
       implements KafkaOuterRequestHandler<MQDataDto, VoidKafkaResponse> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public StoreMQDataRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public VoidKafkaResponse handle(MQDataDto mqData) {
        mqServiceHandler.storeMQData(mqData);
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<MQDataDto> getRequestClass() {
        return MQDataDto.class;
    }

}
