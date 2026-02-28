package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.kafka.dto.SaveTournamentRoundResultRequest;
import com.dgphoenix.casino.kafka.dto.TournamentInfoDto;

@Component
public class SaveTournamentRoundResultRequestHandler 
       implements KafkaOuterRequestHandler<SaveTournamentRoundResultRequest, TournamentInfoDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public SaveTournamentRoundResultRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public TournamentInfoDto handle(SaveTournamentRoundResultRequest request) {
        return mqServiceHandler.saveTournamentRoundResult(request.getAccountId(), request.getSessionId(), request.getGameSessionId(), request.getTournamentId(), request.getBalance(), request.getData(), request.getResult(), request.getRoundId());
    }

    @Override
    public Class<SaveTournamentRoundResultRequest> getRequestClass() {
        return SaveTournamentRoundResultRequest.class;
    }

}
