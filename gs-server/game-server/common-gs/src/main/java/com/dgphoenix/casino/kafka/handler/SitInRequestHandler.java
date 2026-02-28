package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.SitInRequest;
import com.abs.casino.kafka.dto.SitInResponseDto;

@Component
public class SitInRequestHandler 
       implements KafkaOuterRequestHandler<SitInRequest, SitInResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public SitInRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public SitInResponseDto handle(SitInRequest request) {
        return mqServiceHandler.sitIn(request.getSessionId(), request.getGameId(), request.getMode(), request.getLang(), request.getBonusId(), request.getOldGameSessionId(), request.getOldRoundId(), request.getRoomId(), request.getBetNumber(), request.getTournamentId(), request.getNickname());
    }

    @Override
    public Class<SitInRequest> getRequestClass() {
        return SitInRequest.class;
    }

}
