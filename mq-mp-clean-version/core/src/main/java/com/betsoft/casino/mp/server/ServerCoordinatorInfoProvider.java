package com.betsoft.casino.mp.server;

import java.util.Map;

import com.betsoft.casino.mp.service.ServerConfigDto;

public interface ServerCoordinatorInfoProvider {
    public Integer getServerId();
    public boolean isMaster();
    public Integer getMasterServerId();
    public Map<Integer, ServerConfigDto> getServerInfos() throws Exception;
    public Map<Integer, ServerConfigDto> getOnlineServerStates() throws Exception;
    public ServerConfigDto getServerInfo(int serverId) throws Exception;
}
