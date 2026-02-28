package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaInServiceRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.common.cache.LoadBalancerCache;
import com.dgphoenix.casino.kafka.dto.NotifyOnServerStatusesUpdatedRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;


@Component
public class NotifyOnServerStatusesUpdatedRequestHandler implements KafkaInServiceRequestHandler<NotifyOnServerStatusesUpdatedRequest, VoidKafkaResponse> {

    @Autowired
    private LoadBalancerCache loadBalancerCache;

    @Override
    public VoidKafkaResponse handle(NotifyOnServerStatusesUpdatedRequest request) {
        loadBalancerCache.updateServers(request.getChangedServers());
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<NotifyOnServerStatusesUpdatedRequest> getRequestClass() {
        return NotifyOnServerStatusesUpdatedRequest.class;
    }
}
