package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.CloseFRBonusAndSessionRequest;
import com.abs.casino.kafka.dto.CloseFRBonusResultDto;

@Component
public class CloseFRBonusAndSessionRequestHandler
        implements KafkaOuterRequestHandler<CloseFRBonusAndSessionRequest, CloseFRBonusResultDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public CloseFRBonusAndSessionRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public CloseFRBonusResultDto handle(CloseFRBonusAndSessionRequest request) {
        return mqServiceHandler.closeFRBonusAndSession(request.getAccountId(),
                request.getSessionId(), request.getGameSessionId(), request.getGameId(),
                request.getBonusId(), request.getWinSum());
    }

    @Override
    public Class<CloseFRBonusAndSessionRequest> getRequestClass() {
        return CloseFRBonusAndSessionRequest.class;
    }

}
