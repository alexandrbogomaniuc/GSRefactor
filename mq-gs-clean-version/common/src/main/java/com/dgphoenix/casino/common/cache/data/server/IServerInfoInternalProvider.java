package com.dgphoenix.casino.common.cache.data.server;

import java.util.Map;

public interface IServerInfoInternalProvider {

    Map<Long, ServerInfo> getAllServers();

    void persist(ServerInfo serverInfo);

    void remove(Long key);
}
