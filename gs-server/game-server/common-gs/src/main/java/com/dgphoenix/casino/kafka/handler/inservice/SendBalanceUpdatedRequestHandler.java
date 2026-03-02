package com.abs.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.gs.socket.InServiceServiceHandler;
import com.abs.casino.kafka.dto.SendBalanceUpdatedRequest;
import com.abs.casino.kafka.dto.VoidKafkaResponse;
import com.abs.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class SendBalanceUpdatedRequestHandler implements KafkaInServiceAsyncRequestHandler<SendBalanceUpdatedRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public SendBalanceUpdatedRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(SendBalanceUpdatedRequest request) {
        serviceHandler.sendBalanceUpdated(request.getSessionId(), request.getBalance());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<SendBalanceUpdatedRequest> getRequestClass() {
        return SendBalanceUpdatedRequest.class;
    }

}
