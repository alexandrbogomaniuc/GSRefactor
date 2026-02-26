package com.dgphoenix.casino.kafka.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.GetBatchAddWinStatusRequest;
import com.abs.casino.kafka.dto.StringResponseDto;

@Component
public class GetBatchAddWinStatusRequestHandler 
       implements KafkaOuterRequestHandler<GetBatchAddWinStatusRequest, StringResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public GetBatchAddWinStatusRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public StringResponseDto handle(GetBatchAddWinStatusRequest request) {
        return new StringResponseDto(mqServiceHandler.getBatchAddWinStatus(request.getRoomId(), request.getRoundId()));
    }

    @Override
    public Class<GetBatchAddWinStatusRequest> getRequestClass() {
        return GetBatchAddWinStatusRequest.class;
    }

}
