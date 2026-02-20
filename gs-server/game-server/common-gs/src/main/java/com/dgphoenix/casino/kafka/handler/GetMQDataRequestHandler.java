package com.dgphoenix.casino.kafka.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.GetMQDataRequest;
import com.dgphoenix.casino.kafka.dto.MQDataWrapperDto;

@Component
public class GetMQDataRequestHandler 
       implements KafkaOuterRequestHandler<GetMQDataRequest, MQDataWrapperDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public GetMQDataRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public MQDataWrapperDto handle(GetMQDataRequest request) {
        return mqServiceHandler.getMQData(request.getAccountId(), request.getGameId());
    }

    @Override
    public Class<GetMQDataRequest> getRequestClass() {
        return GetMQDataRequest.class;
    }

}
