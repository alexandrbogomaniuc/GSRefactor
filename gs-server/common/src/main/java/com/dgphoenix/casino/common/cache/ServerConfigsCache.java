package com.abs.casino.common.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.abs.casino.common.cache.data.server.IServerInfoInternalProvider;
import com.abs.casino.common.cache.data.server.ServerInfo;
import com.abs.casino.common.config.GameServerConfig;
import com.abs.casino.common.config.GameServerConfigTemplate;
import com.google.common.collect.Sets;

/**
 * User: flsh
 * Date: 10.06.14.
 */

public class ServerConfigsCache {
    private static ServerConfigsCache instance = null;
    private int thisServerId;

    // gameServerId -> GameServerConfig
    private final ConcurrentMap<Integer, GameServerConfig> gsConfigs = new ConcurrentHashMap<>();

    public ServerConfigsCache(IServerInfoInternalProvider serverInfoProvider, int newServerId) {
        Map<Long, ServerInfo> servers = serverInfoProvider.getAllServers();
        List<ServerInfo> serverInfos = new ArrayList<ServerInfo>(servers.values());
        serverInfos.add(new ServerInfo(newServerId));
        refreshServerConfigs(serverInfos);
        this.thisServerId = newServerId;
        instance = this;
    }

    public static ServerConfigsCache getInstance() {
        return Optional.ofNullable(instance)
                .orElseThrow(() -> new RuntimeException("ServerConfigsCache is not yet initialized"));
    }

    public GameServerConfig getServerConfig(int serverId) {
        return gsConfigs.get(serverId);
    }

    public Set<Integer> getServerIds() {
        return Sets.newHashSet(gsConfigs.keySet());
    }

    public GameServerConfig getObject(String id) {
        return gsConfigs.get(Integer.valueOf(id));
    }

    public Map<Integer, GameServerConfig> getAllObjects() {
        return gsConfigs;
    }

    public int size() {
        return gsConfigs.size();
    }

    public void refreshServerConfigs(Collection<ServerInfo> configs) {
        synchronized (gsConfigs) {
            GameServerConfigTemplate template = ServerConfigsTemplateCache.getInstance().getServerConfigTemplate();
            gsConfigs.clear();
            for (ServerInfo info : configs) {
                gsConfigs.put(info.getServerId(), new GameServerConfig(info.getServerId(),
                        info.getLabel(), info.isServerOnline(), info.isMaster(), template));
            }
        }
    }

    public int getThisServerId() {
        return thisServerId;
    }
}
