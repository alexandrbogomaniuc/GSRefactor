package com.abs.casino.kafka.dto;

import java.util.Map;

import com.abs.casino.common.cache.data.server.ServerOnlineStatus;
import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class NotifyOnServerStatusesUpdatedRequest implements KafkaRequest {
    private Map<Integer, ServerOnlineStatus> changedServers;

    public NotifyOnServerStatusesUpdatedRequest() {
        super();
    }

    public NotifyOnServerStatusesUpdatedRequest(Map<Integer, ServerOnlineStatus> changedServers) {
        super();
        this.changedServers = changedServers;
    }

    public Map<Integer, ServerOnlineStatus> getChangedServers() {
        return changedServers;
    }

    public void setChangedServers(Map<Integer, ServerOnlineStatus> changedServers) {
        this.changedServers = changedServers;
    }
}
