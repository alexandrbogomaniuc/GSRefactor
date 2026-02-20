package com.dgphoenix.casino.kafka.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.AddBatchWinRequestDto;
import com.dgphoenix.casino.kafka.dto.AddBatchWinResponseDto;

@Component
public class AddBatchWinRequestHandler 
       implements KafkaOuterRequestHandler<AddBatchWinRequestDto, AddBatchWinResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public AddBatchWinRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public AddBatchWinResponseDto handle(AddBatchWinRequestDto request) {
        return new AddBatchWinResponseDto(mqServiceHandler.addBatchWin(request.getRoomId(), request.getRoundId(), request.getGameId(), request.getAddWinRequest(), request.getTimeoutInMillis()));
    }

    @Override
    public Class<AddBatchWinRequestDto> getRequestClass() {
        return AddBatchWinRequestDto.class;
    }

}
