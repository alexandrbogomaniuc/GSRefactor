package com.dgphoenix.casino.cassandra;

import com.datastax.driver.core.*;
import com.dgphoenix.casino.cassandra.config.ClusterConfig;
import com.dgphoenix.casino.cassandra.config.ColumnFamilyConfig;
import com.dgphoenix.casino.common.configuration.ConfigHelper;
import com.dgphoenix.casino.common.util.NtpTimeProvider;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.09.16
 */
public class KeyspaceConfiguration {

    /**
     * For centralized change protocol version
     */
    public static final ProtocolVersion PROTOCOL_VERSION = ProtocolVersion.V3;

    private final String filename;
    private final ConfigHelper configHelper;
    private final NtpTimeProvider timeProvider;
    private ClusterConfig clusterConfig;
    private ConsistencyLevel readConsistencyLevel;
    private ConsistencyLevel writeConsistencyLevel;
    private ConsistencyLevel serialConsistencyLevel = ConsistencyLevel.LOCAL_SERIAL;

    public KeyspaceConfiguration(String filename, ConfigHelper configHelper, NtpTimeProvider timeProvider) {
        this.filename = filename;
        this.configHelper = configHelper;
        this.timeProvider = timeProvider;
        configHelper.registerAlias(ClusterConfig.class);
    }

    public void load() {
        clusterConfig = (ClusterConfig) configHelper.getConfig(filename);
        checkNotNull(clusterConfig, "Unparsable config file: %s. File may not exists", filename);
        checkState(isNotBlank(clusterConfig.getKeySpace()), "Config must contain keyspace name: %s", filename);
        checkState(!clusterConfig.getParsedHosts().isEmpty(), "Config contains unparsable host list: %s", clusterConfig.getHosts());
        readConsistencyLevel = ConsistencyLevel.valueOf(clusterConfig.getReadConsistencyLevel());
        writeConsistencyLevel = ConsistencyLevel.valueOf(clusterConfig.getWriteConsistencyLevel());
        if (clusterConfig.getSerialConsistencyLevel() != null) {
            serialConsistencyLevel = ConsistencyLevel.valueOf(clusterConfig.getSerialConsistencyLevel());
            checkState(serialConsistencyLevel.isSerial(), "Keyspace serial consistency level can be only SERIAL or LOCAL_SERIAL not %s", serialConsistencyLevel);
        }
    }

    public List<ColumnFamilyConfig> getPersistersConfigs() {
        return clusterConfig.getColumnFamilyConfigs().stream()
                .filter(ColumnFamilyConfig::isEnabled)
                .collect(Collectors.toList());
    }

    public Cluster buildCluster(Cluster.Builder clusterBuilder) {
        clusterBuilder.withClusterName(clusterConfig.getClusterName());
        QueryOptions options = new QueryOptions();
        options.setConsistencyLevel(writeConsistencyLevel);
        clusterBuilder.withQueryOptions(options);
        clusterBuilder.addContactPointsWithPorts(clusterConfig.getParsedHosts()).withProtocolVersion(PROTOCOL_VERSION);
        clusterBuilder.withTimestampGenerator(new NtpTimeGenerator(timeProvider));

        SocketOptions socketOptions = new SocketOptions();
        socketOptions.setConnectTimeoutMillis(10000);
        socketOptions.setReadTimeoutMillis(50000);
        socketOptions.setTcpNoDelay(true);
        socketOptions.setReuseAddress(true);
        socketOptions.setKeepAlive(true);
        clusterBuilder.withSocketOptions(socketOptions);

        PoolingOptions poolingOptions = new PoolingOptions();
        poolingOptions.setMaxConnectionsPerHost(HostDistance.LOCAL, 2);
        poolingOptions.setCoreConnectionsPerHost(HostDistance.LOCAL, 2);
        poolingOptions.setHeartbeatIntervalSeconds(90); //must be > then  SocketOptions.readTimeoutMillis
        //poolingOptions.setPoolTimeoutMillis(10000);
        poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, 8192);
        clusterBuilder.withPoolingOptions(poolingOptions);
        return clusterBuilder.build();
    }

    public ConsistencyLevel getReadConsistencyLevel() {
        return readConsistencyLevel;
    }

    public ConsistencyLevel getWriteConsistencyLevel() {
        return writeConsistencyLevel;
    }

    public ConsistencyLevel getSerialConsistencyLevel() {
        return serialConsistencyLevel;
    }

    public String getKeyspaceName() {
        return clusterConfig.getKeySpace();
    }

    public boolean isCreateSchema() {
        return clusterConfig.isCreateScheme();
    }

    public ClusterConfig getClusterConfig() {
        return clusterConfig;
    }

    public long getMinimumOnlineHosts() {
        return clusterConfig.getMinimumOnlineHosts();
    }

    public String getLocalDataCenterName() {
        return clusterConfig.getLocalDataCenterName();
    }

    public Set<String> getJmxHosts() {
        return clusterConfig.getJmxHosts();
    }

    @Override
    public String toString() {
        return "KeyspaceConfiguration{" +
                "clusterConfig=" + clusterConfig +
                ", readConsistencyLevel=" + readConsistencyLevel +
                ", writeConsistencyLevel=" + writeConsistencyLevel +
                ", filename='" + filename + '\'' +
                '}';
    }
}
