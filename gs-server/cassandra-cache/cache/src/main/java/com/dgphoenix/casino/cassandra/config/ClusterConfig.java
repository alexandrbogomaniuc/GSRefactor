package com.dgphoenix.casino.cassandra.config;

import com.dgphoenix.casino.common.configuration.IXmlConfig;
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
    private String clusterName;
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

    @XStreamOmitField
    private transient Collection<InetSocketAddress> parsedHosts;
    @XStreamOmitField
    private transient Map<String, String> dataCenterReplicationsFactorsMap;
    @XStreamOmitField
    private transient Set<String> parsedJmxHosts;

    public String getClusterName() {
        return clusterName;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ClusterConfig");
        sb.append("[clusterName='").append(clusterName).append('\'');
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
        sb.append(']');
        return sb.toString();
    }
}