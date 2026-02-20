package com.dgphoenix.casino.kafka.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.SitOutTournamentSessionRequest;
import com.dgphoenix.casino.kafka.dto.SitOutTournamentSessionResultDto;

@Component
public class SitOutTournamentSessionRequestHandler implements
        KafkaOuterRequestHandler<SitOutTournamentSessionRequest, SitOutTournamentSessionResultDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public SitOutTournamentSessionRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public SitOutTournamentSessionResultDto handle(SitOutTournamentSessionRequest request) {
        return mqServiceHandler.sitOutTournamentSession(request.getAccountId(),
                request.getSessionId(), request.getGameSessionId(), request.getTournamentId(),
                request.getBalance(), request.getData(), request.getRoundInfo(),
                request.getRoundId());
    }

    @Override
    public Class<SitOutTournamentSessionRequest> getRequestClass() {
        return SitOutTournamentSessionRequest.class;
    }

}
