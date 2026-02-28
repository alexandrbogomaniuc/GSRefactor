package com.abs.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.abs.casino.kafka.dto.BooleanResponseDto;
import com.abs.casino.kafka.dto.RemoteUnlockRequest;
import com.abs.casino.kafka.handler.KafkaInServiceRequestHandler;
import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;

@Component
public class RemoteUnlockRequestHandler
        implements KafkaInServiceRequestHandler<RemoteUnlockRequest, BooleanResponseDto>,
        KafkaOuterRequestHandler<RemoteUnlockRequest, BooleanResponseDto> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public RemoteUnlockRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public BooleanResponseDto handle(RemoteUnlockRequest request) {
        boolean unlocked = serviceHandler.unlock(request.getLockManagerName(), request.getLockId(),
                request.getLockTime());
        return new BooleanResponseDto(unlocked);
    }

    @Override
    public Class<RemoteUnlockRequest> getRequestClass() {
        return RemoteUnlockRequest.class;
    }

}
