package com.betsoft.casino.mp.web.handlers.kafka;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.service.ServerOnlineStatus;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MpServiceHandler {
    private static final Logger LOG = LogManager.getLogger(MpServiceHandler.class);

    private final ServerConfigService serverConfigService;

    @Autowired
    public MpServiceHandler(ServerConfigService serverConfigService) {
        this.serverConfigService = serverConfigService;
    }

    public void updateServersStatuses(Map<Integer, ServerOnlineStatus> changedServers) {
        LOG.info("updateServerStatuses: mpServerId={}",
                changedServers);
        serverConfigService.updateServers(changedServers);
    }
}
