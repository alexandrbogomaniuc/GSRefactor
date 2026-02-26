package com.dgphoenix.casino.gs.status;

import java.util.Map;

import com.dgphoenix.casino.common.cache.LoadBalancerCache;
import com.abs.casino.common.cache.data.server.ServerOnlineStatus;

class WatchServersThreadSlave implements Runnable {

    private final ServersStatusWatcher serversStatusWatcher;
    private final LoadBalancerCache loadBalancerCache;
    private final WatchServersThreadMaster watchServersThreadMaster;

    WatchServersThreadSlave(ServersStatusWatcher serversStatusWatcher, LoadBalancerCache loadBalancerCache, WatchServersThreadMaster watchServersThreadMaster) {
        this.serversStatusWatcher = serversStatusWatcher;
        this.loadBalancerCache = loadBalancerCache;
        this.watchServersThreadMaster = watchServersThreadMaster;
    }

    @Override
    public void run() {
        if (serversStatusWatcher.isMaster()) {
            return;
        }

        Map<Integer, ServerOnlineStatus> changedServers = watchServersThreadMaster.updateServers();
        loadBalancerCache.updateServers(changedServers);
    }
}
