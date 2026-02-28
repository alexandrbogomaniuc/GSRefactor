package com.betsoft.casino.mp.server.status;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.kafka.KafkaMessageService;
import com.betsoft.casino.mp.server.ServerCoordinatorInfoProvider;
import com.betsoft.casino.mp.service.ServerConfigDto;
import com.betsoft.casino.mp.service.ServerOnlineStatus;
import com.abs.casino.kafka.dto.NotifyOnServerStatusesUpdatedRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

class WatchServersThreadMaster implements Runnable {

    private static final Logger LOG = LogManager.getLogger(WatchServersThreadMaster.class);

    private final ServersStatusWatcher serversStatusWatcher;
    private final KafkaMessageService kafkaMessageService;
    private final ServerConfigService serverConfigService;
    private final ServerCoordinatorInfoProvider serverCoordinatorInfoProvider;

    WatchServersThreadMaster(ServersStatusWatcher serversStatusWatcher, KafkaMessageService kafkaMessageService,
                             ServerConfigService serverConfigService, ServerCoordinatorInfoProvider serverCoordinatorInfoProvider) {
        this.serversStatusWatcher = serversStatusWatcher;
        this.kafkaMessageService = kafkaMessageService;
        this.serverConfigService = serverConfigService;
        this.serverCoordinatorInfoProvider = serverCoordinatorInfoProvider;
    }

    @Override
    public void run() {
        if (serversStatusWatcher.getCurrentMasterMpId() < 0) {
            LOG.error("vote master is failed, return");
            return;
        }
        boolean master = serversStatusWatcher.isMaster();

        ServerConfigDto masterConfig = serverConfigService.getConfig(serversStatusWatcher.getCurrentMasterMpId());
        ServerConfigDto thisConfig = serverConfigService.getConfig();
        if (!masterConfig.isMaster()) {
            masterConfig.setIsMaster(true);
            serverConfigService.put(masterConfig);
        } else if (thisConfig.isMaster() && !master) {
            thisConfig.setIsMaster(false);
            serverConfigService.put(thisConfig);
        }

        if (!master) {
            return;
        }

        Map<Integer, ServerOnlineStatus> changedServers = updateServers();

        notifyRemoteListeners(changedServers);
    }

    public  Map<Integer, ServerOnlineStatus> updateServers() {
        Map<Integer, ServerOnlineStatus> changedServers = new HashMap<Integer, ServerOnlineStatus>();

        Map<Integer, ServerConfigDto> existingServers = serverConfigService.getConfigsMap();
        Map<Integer, ServerConfigDto> serversFromCoordinator;
        try {
            serversFromCoordinator = serverCoordinatorInfoProvider.getServerInfos();
        } catch (Exception e) {
            LOG.error("Unable to get server info from coordinator service. Skipping updating servers... ", e);
            return changedServers;
        }

        for (Entry<Integer, ServerConfigDto> entry : existingServers.entrySet()) {
            Integer existingSrvrId = entry.getKey();
            ServerConfigDto serverFromCrdntr = serversFromCoordinator.get(existingSrvrId);
            if (serverFromCrdntr == null) {
                changedServers.put(existingSrvrId, ServerOnlineStatus.REMOVED);
                continue;
            }

            if (serverFromCrdntr.isOnline() != entry.getValue().isOnline()) {
                changedServers.put(existingSrvrId, 
                        serverFromCrdntr.isOnline() 
                        ? ServerOnlineStatus.ONLINE : ServerOnlineStatus.UNAVAILABLE);
                continue;
            }
        }

        Set<Integer> existingServersIds = existingServers.keySet();
        Set<Integer> serversFromCrdntrIds = serversFromCoordinator.keySet();

        serversFromCrdntrIds.removeAll(existingServersIds);

        for (Integer serverId : serversFromCrdntrIds) {
            changedServers.put(serverId, 
                    serversFromCoordinator.get(serverId).isOnline() 
                    ? ServerOnlineStatus.ONLINE : ServerOnlineStatus.UNAVAILABLE);
        }

        LOG.debug("changedServers={}", changedServers);

        serverConfigService.updateServers(changedServers);

        return changedServers;
    }

    private void notifyRemoteListeners(Map<Integer, ServerOnlineStatus> changedServers) {
        if (changedServers.isEmpty()) {
            return;
        }
        Set<Integer> existingServerIds = serverConfigService.getConfigsMap()
                .values().stream()
                .filter(si -> si.isOnline())
                .map(ServerConfigDto::getId).collect(Collectors.toSet());

        NotifyOnServerStatusesUpdatedRequest request = new NotifyOnServerStatusesUpdatedRequest(changedServers);

        for (Integer gsId : existingServerIds) {
            if (gsId == serverCoordinatorInfoProvider.getServerId()) {
                continue;
            }
            kafkaMessageService.asyncRequestToSpecificMP(request, gsId);
        }
    }
}
