package com.abs.casino.common.cache.data.server;

import com.abs.casino.common.cache.data.server.ServerInfo;
import java.util.Map;

public interface IServerInfoInternalProvider {

    Map<Long, ServerInfo> getAllServers();

    void persist(ServerInfo serverInfo);

    void remove(Long key);
}
