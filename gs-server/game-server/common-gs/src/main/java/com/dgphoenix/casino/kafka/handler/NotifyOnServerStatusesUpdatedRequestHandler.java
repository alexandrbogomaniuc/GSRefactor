package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaInServiceRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.common.cache.LoadBalancerCache;
import com.abs.casino.kafka.dto.NotifyOnServerStatusesUpdatedRequest;
import com.abs.casino.kafka.dto.VoidKafkaResponse;


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
