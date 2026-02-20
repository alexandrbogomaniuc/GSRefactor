package com.betsoft.casino.mp.data.service;

import com.betsoft.casino.mp.data.persister.ServerConfigPersister;
import com.betsoft.casino.mp.server.ServerCoordinatorInfoProvider;
import com.betsoft.casino.mp.service.IServerConfigService;
import com.betsoft.casino.mp.service.ServerConfig;
import com.betsoft.casino.mp.service.ServerConfigDto;
import com.betsoft.casino.mp.service.ServerOnlineStatus;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: flsh
 * Date: 20.11.17.
 */
@Service
@DependsOn({"jsonHelper"})
public class ServerConfigService implements IServerConfigService<ServerConfigDto> {
    public static final Logger LOG = LogManager.getLogger(ServerConfigService.class);
    public static final String MP_SERVER_ID = "MP_SERVER_ID";
    private final ServerConfigPersister serverConfigPersister;
    private final ServerCoordinatorInfoProvider serverCoordinatorInfoProvider;
    private final ConcurrentMap<Integer, ServerConfigDto> mpServerConfigs = new ConcurrentHashMap<>();

    private final int thisServerId;

    @Value("${mpserver.domain}")
    private String mpServerDomain;

    @Value("${mpserver.domain.servername}")
    private String serverNameTemplate;

    public ServerConfigService(ServerConfigPersister serverConfigPersister,
                               ServerCoordinatorInfoProvider serverCoordinatorInfoProvider) {
        this.serverConfigPersister = serverConfigPersister;
        this.serverCoordinatorInfoProvider = serverCoordinatorInfoProvider;

        Integer mpServerId = serverCoordinatorInfoProvider.getServerId();
        if (mpServerId == null) {
            throw new RuntimeException("ServerIdLockerService does not provide 'serverId'");
        } else {
            this.thisServerId = mpServerId;
        }
    }

    @PostConstruct
    public void init() {
        LOG.debug("Starting with serverId={}", thisServerId);
        try {
            Map<Integer, ServerConfigDto> servers = serverCoordinatorInfoProvider.getServerInfos();
            for (Entry<Integer, ServerConfigDto> server : servers.entrySet()) {
                put(server.getValue());
            }
        } catch (Exception e) {
            LOG.error("Failed to get info about servers from server coordinator", e);
        }
        LOG.debug("Started with serverId={}, config={}", thisServerId, getConfig());
        try {
            put(serverCoordinatorInfoProvider.getServerInfo(thisServerId));
        } catch (Exception e) {
            LOG.error("Error getting server info for serverId=" + thisServerId, e);
            put(serverConfigToDto(new ServerConfig(thisServerId), true));
        }
    }

    @Override
    public int getServerId() {
        return thisServerId;
    }

    @Override
    public ServerConfigDto getConfig() {
        return getConfig(thisServerId);
    }

    @Override
    public ServerConfigDto getConfig(int id) {
        return mpServerConfigs.get(id);
    }

    @Override
    public Iterable<ServerConfigDto> getConfigs() {
        return mpServerConfigs.values();
    }

    @Override
    public Map<Integer, ServerConfigDto> getConfigsMap() {
        return mpServerConfigs;
    }

    @Override
    public void put(ServerConfigDto config) {
        mpServerConfigs.put(config.getId(), config);
        serverConfigPersister.store(config.getId(), new ServerConfig(config.getId()));
        LOG.debug("put: {}", config);
    }

    private ServerConfigDto serverConfigToDto(ServerConfig config, boolean isOnline) {
        ServerConfigDto configDto = serverConfigToDto(config);
        configDto.setOnline(isOnline);
        return configDto;
    }

    private ServerConfigDto serverConfigToDto(ServerConfig config) {
        ServerConfigDto configDto = new ServerConfigDto(config.getServerId());

        String mpServerHost = StringUtils.isTrimmedEmpty(serverNameTemplate) ? "localhost" :
            serverNameTemplate.replace("#", String.valueOf(config.getServerId())) + mpServerDomain;
        configDto.setOldHost(mpServerHost);
        configDto.setDomain(mpServerDomain);
        return configDto;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServerConfigService [");
        sb.append("serverId=").append(thisServerId);
        sb.append(']');
        return sb.toString();
    }

    public void updateServers(Map<Integer, ServerOnlineStatus> changedServers) {
        for (Entry<Integer, ServerOnlineStatus> chServer : changedServers.entrySet()) {
            Integer chServerId = chServer.getKey();

            if (chServerId == thisServerId) {
                continue;
            }

            ServerConfigDto registeredServer = mpServerConfigs.get(chServerId);

            switch (chServer.getValue()) {
                case ONLINE:
                    try {
                        registeredServer = serverCoordinatorInfoProvider.getServerInfo(chServerId);
                    } catch (Exception e) {
                        LOG.error("Error getting server info for server " + chServerId, e);
                        if (registeredServer == null) {
                            registeredServer = new ServerConfigDto(chServerId);
                        }
                        registeredServer.setOnline(true);
                    }
                    mpServerConfigs.put(chServerId, registeredServer);
                    break;
                case REMOVED:
                    serverConfigPersister.delete(chServerId);
                    mpServerConfigs.remove(chServerId);
                    break;
                case UNAVAILABLE:
                    if (registeredServer == null) {
                        registeredServer = new ServerConfigDto(chServerId);
                    }
                    registeredServer.setOnline(false);
                    mpServerConfigs.put(chServerId, registeredServer);
                    break;
                default:
                    break;
            }
        }
        
    }

    @Override
    public boolean isThisIsAMaster() {
        return serverCoordinatorInfoProvider.isMaster();
    }
}
