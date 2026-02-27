package com.abs.casino.gs.status;

import com.dgphoenix.casino.common.cache.LoadBalancerCache;
import com.dgphoenix.casino.common.cache.data.server.ServerCoordinatorInfoProvider;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.abs.casino.common.cache.data.server.ServerOnlineStatus;
import com.dgphoenix.casino.gs.IGameServerStatusListener;
import com.dgphoenix.casino.kafka.dto.NotifyOnServerStatusesUpdatedRequest;
import com.abs.casino.kafka.service.KafkaMessageService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

class WatchServersThreadMaster implements Runnable {

    private static final Logger LOG = LogManager.getLogger(WatchServersThreadMaster.class);

    private final ServersStatusWatcher serversStatusWatcher;
    private final KafkaMessageService kafkaMessageService;
    private final LoadBalancerCache loadBalancerCache;
    private final ServerCoordinatorInfoProvider serverCoordinatorInfoProvider;

    WatchServersThreadMaster(ServersStatusWatcher serversStatusWatcher,
                             KafkaMessageService kafkaMessageService,
                             LoadBalancerCache loadBalancerCache,
                             ServerCoordinatorInfoProvider serverCoordinatorInfoProvider) {
        this.serversStatusWatcher = serversStatusWatcher;
        this.kafkaMessageService = kafkaMessageService;
        this.loadBalancerCache = loadBalancerCache;
        this.serverCoordinatorInfoProvider = serverCoordinatorInfoProvider;
    }

    @Override
    public void run() {
        if (serversStatusWatcher.getCurrentMasterGsId() < 0) {
            LOG.error("vote master is failed, return");
            return;
        }
        boolean master = serversStatusWatcher.isMaster();

        ServerInfo masterConfig = loadBalancerCache.getServerInfoById(serverCoordinatorInfoProvider.getMasterServerId());
        ServerInfo thisConfig = loadBalancerCache.getCurrentServerInfo();
        if (!masterConfig.isMaster()) {
            masterConfig.setIsMaster(true);
            loadBalancerCache.put(masterConfig);
        } else if (thisConfig.isMaster() && !master) {
            thisConfig.setIsMaster(false);
            loadBalancerCache.put(thisConfig);
        }

        if (!master) {
            return;
        }

        Map<Integer, ServerOnlineStatus> changedServers = updateServers();

        loadBalancerCache.updateServers(changedServers);

        notifyListeners(changedServers);
    }

    private void notifyListeners(Map<Integer, ServerOnlineStatus> changedServers) {
        Map<Integer, Boolean> changedStatuses = changedServers.entrySet()
                .stream()
                .filter(e -> !e.getValue().equals(ServerOnlineStatus.REMOVED))
                .collect(Collectors.toMap(Entry::getKey, (e) -> e.getValue().equals(ServerOnlineStatus.ONLINE)));

        //notify local (this GS) listeners
        for (Map.Entry<Integer, Boolean> idAndOnline : changedStatuses.entrySet()) {
            for (IGameServerStatusListener listener : serversStatusWatcher.getServerStatusListeners()) {
                try {
                    listener.notify(idAndOnline.getKey(), idAndOnline.getValue());
                } catch (Exception e) {
                    LOG.warn("notify error:", e);
                }
            }
        }

        notifyRemoteListeners(changedServers);
    }

    public Map<Integer, ServerOnlineStatus> updateServers() {
        Map<Integer, ServerOnlineStatus> changedServers = new HashMap<Integer, ServerOnlineStatus>();

        Map<Integer, ServerInfo> existingServers = loadBalancerCache.getAllObjects();
        Map<Integer, ServerInfo> serversFromCoordinator;
        try {
            serversFromCoordinator = serverCoordinatorInfoProvider.getServerInfos();
        } catch (Exception e) {
            LOG.error("Unable to get server info from coordinator service. Skipping updating servers... ", e);
            return changedServers;
        }

        for (Entry<Integer, ServerInfo> entry : existingServers.entrySet()) {
            Integer existingSrvrId = entry.getKey();
            ServerInfo serverFromCrdntr = serversFromCoordinator.get(existingSrvrId);
            if (serverFromCrdntr == null) {
                changedServers.put(existingSrvrId, ServerOnlineStatus.REMOVED);
                continue;
            }

            if (serverFromCrdntr.isServerOnline() != entry.getValue().isServerOnline()) {
                changedServers.put(existingSrvrId, 
                        serverFromCrdntr.isServerOnline() 
                        ? ServerOnlineStatus.ONLINE : ServerOnlineStatus.UNAVAILABLE);
                continue;
            }
        }

        Set<Integer> existingServersIds = existingServers.keySet();
        Set<Integer> serversFromCrdntrIds = serversFromCoordinator.keySet();

        serversFromCrdntrIds.removeAll(existingServersIds);

        for (Integer serverId : serversFromCrdntrIds) {
            changedServers.put(serverId, 
                    serversFromCoordinator.get(serverId).isServerOnline() 
                    ? ServerOnlineStatus.ONLINE : ServerOnlineStatus.UNAVAILABLE);
        }

        LOG.debug("changedServers={}", changedServers);
        return changedServers;
    }

    private void notifyRemoteListeners(Map<Integer, ServerOnlineStatus> changedServers) {
        if (changedServers.isEmpty()) {
            return;
        }
        Set<Integer> existingServerIds = loadBalancerCache.getAllObjects()
                .values().stream()
                .filter(si -> si.isServerOnline())
                .map(ServerInfo::getServerId).collect(Collectors.toSet());

        NotifyOnServerStatusesUpdatedRequest request = new NotifyOnServerStatusesUpdatedRequest(changedServers);

        for (Integer gsId : existingServerIds) {
            if (gsId == serverCoordinatorInfoProvider.getServerId()) {
                continue;
            }
            kafkaMessageService.asyncRequestToSpecificGS(request, gsId);
        }
    }
}
