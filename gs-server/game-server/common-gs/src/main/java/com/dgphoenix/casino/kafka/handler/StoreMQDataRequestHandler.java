package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.MQDataDto;
import com.abs.casino.kafka.dto.VoidKafkaResponse;

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
