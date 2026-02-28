package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.BooleanResponseDto;
import com.dgphoenix.casino.kafka.dto.SessionTouchRequest;

@Component
public class SessionTouchRequestHandler 
       implements KafkaOuterRequestHandler<SessionTouchRequest, BooleanResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public SessionTouchRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public BooleanResponseDto handle(SessionTouchRequest request) {
        return new BooleanResponseDto(mqServiceHandler.touchSession(request.getSessionId()));
    }

    @Override
    public Class<SessionTouchRequest> getRequestClass() {
        return SessionTouchRequest.class;
    }

}
