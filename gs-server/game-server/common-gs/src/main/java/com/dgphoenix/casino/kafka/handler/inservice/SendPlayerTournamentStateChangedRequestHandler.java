package com.abs.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.gs.socket.InServiceServiceHandler;
import com.abs.casino.kafka.dto.SendPlayerTournamentStateChangedRequest;
import com.abs.casino.kafka.dto.VoidKafkaResponse;
import com.abs.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class SendPlayerTournamentStateChangedRequestHandler
        implements KafkaInServiceAsyncRequestHandler<SendPlayerTournamentStateChangedRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public SendPlayerTournamentStateChangedRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(SendPlayerTournamentStateChangedRequest request) {
        serviceHandler.sendPlayerTournamentStateChanged(request.getSessionId(),
                request.getTournamentId(), request.isCannotJoin(), request.isJoined());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<SendPlayerTournamentStateChangedRequest> getRequestClass() {
        return SendPlayerTournamentStateChangedRequest.class;
    }

}
