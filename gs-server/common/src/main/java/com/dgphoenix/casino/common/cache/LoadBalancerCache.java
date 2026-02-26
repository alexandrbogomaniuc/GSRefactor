package com.dgphoenix.casino.common.cache;

import com.dgphoenix.casino.common.ILoadBalancer;
import com.abs.casino.common.cache.data.server.IServerInfoInternalProvider;
import com.dgphoenix.casino.common.cache.data.server.ServerCoordinatorInfoProvider;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.cache.data.server.ServerOnlineStatus;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.RNG;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@CacheKeyInfo(description = "registeredServer.id")
public class LoadBalancerCache extends AbstractExportableCache<ServerInfo> implements ILoadBalancer, IServerStatusProvider {
    private static final Logger LOG = LogManager.getLogger(LoadBalancerCache.class);

    // serverId->ServerInfo
    private final Map<Integer, ServerInfo> registeredServers = new ConcurrentHashMap<>();
    private final Set<Integer> knownServers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final IServerInfoInternalProvider serverInfoProvider;
    private final ServerCoordinatorInfoProvider serverCoordinatorInfoProvider;

    public LoadBalancerCache(IServerInfoInternalProvider serverInfoProvider,
                             ServerCoordinatorInfoProvider serverCoordinatorInfoProvider, ServerInfo thisServerInfo) {
        this.serverInfoProvider = serverInfoProvider;
        this.serverCoordinatorInfoProvider = serverCoordinatorInfoProvider;
        put(thisServerInfo);
    }

    @PostConstruct
    private void init() {
        initializeServers();
    }

    /**
     * @deprecated Should be used only for backward compatibility.
     */
    public static LoadBalancerCache getInstance() {
        return ApplicationContextHelper.getApplicationContext()
                .getBean("loadBalancerCache", LoadBalancerCache.class);
    }

    @Override
    public void put(ServerInfo serverInfo) {
        registeredServers.putIfAbsent(serverInfo.getServerId(), serverInfo);
        knownServers.add(serverInfo.getServerId());
    }

    public void initializeServers() {
        initializeRegisteredServers();
        setKnownServers();
        updateServerConfigs();
    }

    public void updateServers(Map<Integer, ServerOnlineStatus> changedServers) {
        updateRegisteredServers(changedServers);
        setKnownServers();
        updateServerConfigs();
    }

    public boolean isExist(int serverId) {
        return this.registeredServers.containsKey(serverId);
    }

    public ServerInfo getServerInfoById(int serverId) {
        return registeredServers.get(serverId);
    }

    public ServerInfo getCurrentServerInfo() {
        return registeredServers.get(serverCoordinatorInfoProvider.getServerId());
    }

    @Override
    public Long getServerUpdateTime(int serverId) {
        ServerInfo serverInfo = getServerInfoById(serverId);
        return serverInfo == null ? null : serverInfo.getUpdateTime();
    }

    @Override
    public Long getStartTime(int serverId) {
        ServerInfo serverInfo = getServerInfoById(serverId);
        return serverInfo == null ? null : serverInfo.getStartTime();
    }

    public ServerInfo assignRandomServer() {
        Set<Integer> servers = this.getServers(true, false);
        Integer[] serversIds = servers.toArray(new Integer[servers.size()]);
        return this.getServerInfoById(serversIds[RNG.nextInt(0, serversIds.length)]);
    }

    public Set<Integer> getServers(Boolean online, Boolean locked) {
        return getServers(online, locked, ServerInfo.ALIVE_TIMEOUT);
    }

    public Set<Integer> getServers(Boolean online, Boolean locked, long timeout) {
        Set<Integer> servers = new HashSet<>();
        List<Integer> serverIds = new ArrayList<>(registeredServers.keySet());
        for (Integer serverId : serverIds) {
            ServerInfo serverInfo = registeredServers.get(serverId);
            if (online != null && locked != null) {
                if (online == serverInfo.isServerOnline() && locked == serverInfo.isLocked()) {
                    servers.add(serverInfo.getServerId());
                }
            } else {
                if (online == null) {
                    if (locked == serverInfo.isLocked()) {
                        servers.add(serverInfo.getServerId());
                    }
                } else {
                    if (online == serverInfo.isServerOnline()) {
                        servers.add(serverInfo.getServerId());
                    }
                }
            }
        }
        return servers;
    }

    public Set<ServerInfo> getServersInfo(Boolean online, Boolean locked) {
        Set<ServerInfo> servers = new HashSet<>();
        List<Integer> serverIds = new ArrayList<>(registeredServers.keySet());
        for (Integer serverId : serverIds) {
            ServerInfo serverInfo = registeredServers.get(serverId);
            LOG.debug("LoadBalancerCache:getServersInfo serverId:" + serverInfo.getServerId() +
                    " online:" + serverInfo.getMaxLoad() + " label:" + serverInfo.getLabel());
            if (online != null && locked != null) {
                if (online == serverInfo.isServerOnline() && locked == serverInfo.isLocked()) {
                    servers.add(serverInfo);
                }
            } else {
                if (online == null && locked == null) {
                    servers.add(serverInfo);
                } else {
                    if (online == null) {
                        if (locked == serverInfo.isLocked()) {
                            servers.add(serverInfo);
                        }
                    } else {
                        if (online == serverInfo.isServerOnline()) {
                            servers.add(serverInfo);
                        }
                    }
                }
            }
        }
        return servers;
    }

    public void clear() {
        registeredServers.clear();
    }

    private void updateRegisteredServers(Map<Integer, ServerOnlineStatus> changedServers) {
        for (Entry<Integer, ServerOnlineStatus> chServer : changedServers.entrySet()) {
            Integer chServerId = chServer.getKey();

            ServerInfo registeredServer = registeredServers.get(chServerId);

            switch (chServer.getValue()) {
                case ONLINE:
                    if (registeredServer == null) {
                        registeredServer = new ServerInfo(chServerId, false);
                    }
                    registeredServer.setOnline(true);
                    registeredServers.put(chServerId, registeredServer);
                    break;
                case REMOVED:
                    serverInfoProvider.remove((long)chServerId);
                    registeredServers.remove(chServerId);
                    break;
                case UNAVAILABLE:
                    if (registeredServer == null) {
                        registeredServer = new ServerInfo(chServerId, false);
                    }
                    registeredServer.setOnline(false);
                    registeredServers.put(chServerId, registeredServer);
                    break;
                default:
                    break;
            }
        }
    }

    private void initializeRegisteredServers() {
        Map<Integer, ServerInfo> serverInfosFromCrdntr = null;
        try {
            serverInfosFromCrdntr = serverCoordinatorInfoProvider.getServerInfos();
        } catch (Exception e) {
            LOG.error("Failed to get info from server coordinator service. Skipping updating servers.", e);
            return;
        }

        for (Entry<Integer, ServerInfo> entry : serverInfosFromCrdntr.entrySet()) {
            registeredServers.computeIfAbsent(entry.getKey(), (k) -> {
                return entry.getValue();
            });
        }
    }

    public void setKnownServers() {
        knownServers.retainAll(registeredServers.keySet());
    }

    private void updateServerConfigs() {
        ServerConfigsCache.getInstance().refreshServerConfigs(registeredServers.values());
    }

    public Set<Integer> getServers() {
        return knownServers;
    }

    @Override
    public boolean isOnline(int serverId) {
        ServerInfo serverInfo = getServerInfoById(serverId);
        return serverInfo != null && serverInfo.isServerOnline();
    }

    public void lock(int serverId, boolean lock) {
        if (isOnline(serverId)) {
            ServerInfo server = getServerInfoById(serverId);
            server.setLocked(lock);
        }
    }

    public boolean isAllServersOnline() {
        Set<ServerInfo> servers = getServersInfo(true, null);
        return servers.size() == this.registeredServers.size();
    }

    @Override
    public int size() {
        return registeredServers.size();
    }

    @Override
    public ServerInfo getObject(String id) {
        return registeredServers.get(Integer.valueOf(id));
    }

    @Override
    public Map<Integer, ServerInfo> getAllObjects() {
        return registeredServers;
    }

    @Override
    public String getAdditionalInfo() {
        return NO_INFO;
    }

    @Override
    public String printDebug() {
        return "registeredServers.size()=" + registeredServers.size();
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream) throws IOException {
        synchronized (registeredServers) {
            Collection<Map.Entry<Integer, ServerInfo>> entries = registeredServers.entrySet();
            for (Map.Entry<Integer, ServerInfo> entry : entries) {
                outStream.writeObject(new ExportableCacheEntry(String.valueOf(entry.getKey()), entry.getValue()));
            }
        }
    }

}
