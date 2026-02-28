package com.abs.casino.cassandra.config;

import com.abs.casino.common.configuration.IXmlConfig;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;

/**
 * User: flsh
 * Date: 19.10.11
 */
@XStreamAlias("ClusterConfig")
public class ClusterConfig implements IXmlConfig {
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 10000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 50000;
    private static final boolean DEFAULT_TCP_NO_DELAY = true;
    private static final boolean DEFAULT_REUSE_ADDRESS = true;
    private static final boolean DEFAULT_KEEP_ALIVE = true;
    private static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 2;
    private static final int DEFAULT_CORE_CONNECTIONS_PER_HOST = 2;
    private static final int DEFAULT_HEARTBEAT_INTERVAL_SECONDS = 90;
    private static final int DEFAULT_MAX_REQUESTS_PER_CONNECTION = 8192;

    private String clusterName;
    private Boolean validateClusterName;
    private String keySpace;
    private int hostTimeoutWindow;
    private String replicationStrategyClass;
    private String readConsistencyLevel;
    private String writeConsistencyLevel;
    private String serialConsistencyLevel;
    private int replicationFactor;
    private boolean createScheme;
    private String hosts;
    private long minimumOnlineHosts;
    private String localDataCenterName;
    @XStreamAlias("columnFamilies")
    private List<ColumnFamilyConfig> columnFamilyConfigs;
    @XStreamAlias("dcReplicationFactors")
    private String dataCenterReplicationFactors;
    private String jmxHosts;
    private Integer connectTimeoutMillis;
    private Integer readTimeoutMillis;
    private Boolean tcpNoDelay;
    private Boolean reuseAddress;
    private Boolean keepAlive;
    private Integer maxConnectionsPerHost;
    private Integer coreConnectionsPerHost;
    private Integer heartbeatIntervalSeconds;
    private Integer maxRequestsPerConnection;
    private Boolean enableDcAwareLoadBalancing;

    @XStreamOmitField
    private transient Collection<InetSocketAddress> parsedHosts;
    @XStreamOmitField
    private transient Map<String, String> dataCenterReplicationsFactorsMap;
    @XStreamOmitField
    private transient Set<String> parsedJmxHosts;

    public String getClusterName() {
        return clusterName;
    }

    public boolean isValidateClusterName() {
        return validateClusterName == null ? true : validateClusterName;
    }

    public String getKeySpace() {
        return keySpace;
    }

    public String getReplicationStrategyClass() {
        return replicationStrategyClass;
    }

    public String getReadConsistencyLevel() {
        return readConsistencyLevel;
    }

    public String getWriteConsistencyLevel() {
        return writeConsistencyLevel;
    }

    public String getSerialConsistencyLevel() {
        return serialConsistencyLevel;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public boolean isCreateScheme() {
        return createScheme;
    }

    public String getHosts() {
        return hosts;
    }

    public List<ColumnFamilyConfig> getColumnFamilyConfigs() {
        return columnFamilyConfigs;
    }

    public long getMinimumOnlineHosts() {
        return minimumOnlineHosts;
    }

    public String getLocalDataCenterName() {
        return localDataCenterName;
    }

    public Collection<InetSocketAddress> getParsedHosts() {
        if (parsedHosts == null) {
            Map<String, String> map = CollectionUtils.stringToMap(hosts, ",", ":");
            if (map.isEmpty()) {
                parsedHosts = Collections.emptySet();
            } else {
                Collection<InetSocketAddress> m = new HashSet<>(map.size());
                for (Entry<String, String> entry : map.entrySet()) {
                    InetSocketAddress host = new InetSocketAddress(entry.getKey(), Integer.valueOf(entry.getValue()));
                    m.add(host);
                }
                parsedHosts = m;
            }
        }
        return parsedHosts;
    }

    public Map<String, String> getDataCenterReplicationFactor() {
        if (dataCenterReplicationsFactorsMap == null) {
            dataCenterReplicationsFactorsMap = CollectionUtils.stringToMap(dataCenterReplicationFactors, ",", ":");
        }
        return dataCenterReplicationsFactorsMap;
    }

    public Set<String> getJmxHosts() {
        if (parsedJmxHosts == null) {
            parsedJmxHosts = new HashSet<>(CollectionUtils.stringToListOfStrings(jmxHosts));
        }
        return parsedJmxHosts;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis == null ? DEFAULT_CONNECT_TIMEOUT_MS : connectTimeoutMillis;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis == null ? DEFAULT_READ_TIMEOUT_MS : readTimeoutMillis;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay == null ? DEFAULT_TCP_NO_DELAY : tcpNoDelay;
    }

    public boolean isReuseAddress() {
        return reuseAddress == null ? DEFAULT_REUSE_ADDRESS : reuseAddress;
    }

    public boolean isKeepAlive() {
        return keepAlive == null ? DEFAULT_KEEP_ALIVE : keepAlive;
    }

    public int getMaxConnectionsPerHost() {
        return maxConnectionsPerHost == null ? DEFAULT_MAX_CONNECTIONS_PER_HOST : maxConnectionsPerHost;
    }

    public int getCoreConnectionsPerHost() {
        return coreConnectionsPerHost == null ? DEFAULT_CORE_CONNECTIONS_PER_HOST : coreConnectionsPerHost;
    }

    public int getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds == null ? DEFAULT_HEARTBEAT_INTERVAL_SECONDS : heartbeatIntervalSeconds;
    }

    public int getMaxRequestsPerConnection() {
        return maxRequestsPerConnection == null ? DEFAULT_MAX_REQUESTS_PER_CONNECTION : maxRequestsPerConnection;
    }

    public boolean isEnableDcAwareLoadBalancing() {
        return enableDcAwareLoadBalancing != null && enableDcAwareLoadBalancing;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ClusterConfig");
        sb.append("[clusterName='").append(clusterName).append('\'');
        sb.append(", validateClusterName=").append(isValidateClusterName());
        sb.append(", keySpace='").append(keySpace).append('\'');
        sb.append(", hostTimeoutWindow=").append(hostTimeoutWindow);
        sb.append(", replicationStrategyClass='").append(replicationStrategyClass).append('\'');
        sb.append(", readConsistencyLevel='").append(readConsistencyLevel).append('\'');
        sb.append(", writeConsistencyLevel='").append(writeConsistencyLevel).append('\'');
        sb.append(", replicationFactor=").append(replicationFactor);
        sb.append(", createScheme=").append(createScheme);
        sb.append(", columnFamilyConfigs=").append(columnFamilyConfigs);
        sb.append(", hosts=").append(hosts == null ? "null" : hosts);
        sb.append(", jmxHosts=").append(jmxHosts == null ? "null" : jmxHosts);
        sb.append(", connectTimeoutMillis=").append(getConnectTimeoutMillis());
        sb.append(", readTimeoutMillis=").append(getReadTimeoutMillis());
        sb.append(", tcpNoDelay=").append(isTcpNoDelay());
        sb.append(", reuseAddress=").append(isReuseAddress());
        sb.append(", keepAlive=").append(isKeepAlive());
        sb.append(", maxConnectionsPerHost=").append(getMaxConnectionsPerHost());
        sb.append(", coreConnectionsPerHost=").append(getCoreConnectionsPerHost());
        sb.append(", heartbeatIntervalSeconds=").append(getHeartbeatIntervalSeconds());
        sb.append(", maxRequestsPerConnection=").append(getMaxRequestsPerConnection());
        sb.append(", enableDcAwareLoadBalancing=").append(isEnableDcAwareLoadBalancing());
        sb.append(']');
        return sb.toString();
    }
}
