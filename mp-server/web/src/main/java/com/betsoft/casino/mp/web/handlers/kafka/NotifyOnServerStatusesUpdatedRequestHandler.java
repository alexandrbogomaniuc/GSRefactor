package com.betsoft.casino.mp.web.handlers.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.kafka.dto.NotifyOnServerStatusesUpdatedRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaInServiceRequestHandler;

@Component
public class NotifyOnServerStatusesUpdatedRequestHandler implements KafkaInServiceRequestHandler<NotifyOnServerStatusesUpdatedRequest, VoidKafkaResponse> {
    private final MpServiceHandler mpServiceHandler;

    @Autowired
    public NotifyOnServerStatusesUpdatedRequestHandler(MpServiceHandler mpServiceHandler) {
        this.mpServiceHandler = mpServiceHandler;
    }

    @Override
    public VoidKafkaResponse handle(NotifyOnServerStatusesUpdatedRequest request) {
        mpServiceHandler.updateServersStatuses(request.getChangedServers());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<NotifyOnServerStatusesUpdatedRequest> getRequestClass() {
        return NotifyOnServerStatusesUpdatedRequest.class;
    }
}
