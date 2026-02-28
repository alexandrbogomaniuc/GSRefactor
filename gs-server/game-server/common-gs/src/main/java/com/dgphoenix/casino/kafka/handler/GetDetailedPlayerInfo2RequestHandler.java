package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.DetailedPlayerInfo2Dto;
import com.dgphoenix.casino.kafka.dto.GetDetailedPlayerInfo2Request;

@Component
public class GetDetailedPlayerInfo2RequestHandler 
       implements KafkaOuterRequestHandler<GetDetailedPlayerInfo2Request, DetailedPlayerInfo2Dto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public GetDetailedPlayerInfo2RequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public DetailedPlayerInfo2Dto handle(GetDetailedPlayerInfo2Request request) {
        return mqServiceHandler.getDetailedPlayerInfo2(request.getSessionId(), request.getGameId(), request.getMode(), request.getBonusId(), request.getTournamentId());
    }

    @Override
    public Class<GetDetailedPlayerInfo2Request> getRequestClass() {
        return GetDetailedPlayerInfo2Request.class;
    }

}
