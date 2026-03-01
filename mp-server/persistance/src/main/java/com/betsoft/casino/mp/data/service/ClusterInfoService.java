package com.betsoft.casino.mp.data.service;

import com.abs.casino.common.ILoadBalancer;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * User: flsh
 * Date: 11.04.18.
 */
@Service
public class ClusterInfoService implements ILoadBalancer {
    private static final Logger LOG = LogManager.getLogger(ClusterInfoService.class);
    public static final String START_TIME = "START_TIME";

    private final HazelcastInstance hazelcastInstance;

    public ClusterInfoService(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    //not required
    @Override
    public Long getServerUpdateTime(int serverId) {
        return hazelcastInstance.getCluster().getClusterTime();
    }

    @Override
    public boolean isOnline(int serverId) {
        Member server = findServerById(serverId);
        return server != null;
    }

    @Override
    public Long getStartTime(int serverId) {
        Member server = findServerById(serverId);
        if (server != null) {
            return server.getLongAttribute(START_TIME);
        }
        return null;
    }

    private Member findServerById(int serverId) {
        for (Member member : getMembers()) {
            Integer memberServerId = member.getIntAttribute(ServerConfigService.MP_SERVER_ID);
            if (memberServerId != null && memberServerId == serverId) {
                return member;
            }
        }
        return null;
    }

    private Set<Member> getMembers() {
        return hazelcastInstance.getCluster().getMembers();
    }
}
