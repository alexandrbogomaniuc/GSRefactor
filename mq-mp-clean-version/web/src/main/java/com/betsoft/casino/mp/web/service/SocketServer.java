package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.service.ServerConfigDto;
import com.hazelcast.core.*;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;


@Service
@DependsOn("KafkaMessageService")
public class SocketServer implements MembershipListener {
    private static final Logger LOG = LogManager.getLogger(SocketServer.class);

    @Value("${mpserver.domain}")
    private String mpServerDomain;

    @Value("${mpserver.domain.servername}")
    private String serverNameTemplate;

    private int serverId;

    private ServerConfigService serverConfigService;

    public SocketServer(ServerConfigService serverConfigService) {
        this.serverConfigService = serverConfigService;
    }

    @PostConstruct
    private void init() {
        serverId = serverConfigService.getServerId();
        LOG.info("init: server started with id '{}'", serverId);
    }

    public ServerConfigDto getConfig(int id) {
        return serverConfigService.getConfig(id);
    }

    public int getServerId() {
        return serverConfigService.getServerId();
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        LOG.warn("New HZ server added: {}", membershipEvent);
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        LOG.warn("New HZ server removed: {}", membershipEvent);
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        LOG.warn("HZ server attribute changed: {}", memberAttributeEvent);
    }

    public boolean isOnline(int serverId) {
        if (serverId == serverConfigService.getServerId()) {
            return true;
        }

        ServerConfigDto server = serverConfigService.getConfig(serverId);
        return server != null && server.isOnline();
    }
}
