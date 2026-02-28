package com.abs.casino.common.cache.data.server;

import java.util.Map;

public interface ServerCoordinatorInfoProvider {
    public Integer getServerId();
    public boolean isMaster();
    public Integer getMasterServerId();
    public Map<Integer, ServerInfo> getServerInfos() throws Exception;
    public Map<Integer, ServerInfo> getOnlineServerStates() throws Exception;
}
