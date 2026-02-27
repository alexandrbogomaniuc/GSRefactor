package com.abs.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.dgphoenix.casino.kafka.dto.UpdateStubBalanceByExternalUserIdRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class UpdateStubBalanceByExternalUserIdRequestHandler implements KafkaInServiceAsyncRequestHandler<UpdateStubBalanceByExternalUserIdRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public UpdateStubBalanceByExternalUserIdRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(UpdateStubBalanceByExternalUserIdRequest request) {
        serviceHandler.updateStubBalanceByExternalUserId(request.getExternalUserId(), request.getBalance());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<UpdateStubBalanceByExternalUserIdRequest> getRequestClass() {
        return UpdateStubBalanceByExternalUserIdRequest.class;
    }

}
