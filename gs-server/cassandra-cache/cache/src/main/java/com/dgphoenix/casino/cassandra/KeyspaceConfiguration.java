package com.abs.casino.cassandra;

import com.abs.casino.cassandra.config.ClusterConfig;
import com.abs.casino.cassandra.config.ColumnFamilyConfig;
import com.abs.casino.common.configuration.ConfigHelper;
import com.abs.casino.common.util.NtpTimeProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger LOG = LogManager.getLogger(KeyspaceConfiguration.class);

    private final String filename;
    private final ConfigHelper configHelper;
    private final NtpTimeProvider timeProvider;
    private ClusterConfig clusterConfig;
    private com.datastax.driver.core.ConsistencyLevel readConsistencyLevel;
    private com.datastax.driver.core.ConsistencyLevel writeConsistencyLevel;
    private com.datastax.driver.core.ConsistencyLevel serialConsistencyLevel = com.datastax.driver.core.ConsistencyLevel.LOCAL_SERIAL;

    public KeyspaceConfiguration(String filename, ConfigHelper configHelper, NtpTimeProvider timeProvider) {
        this.filename = filename;
        this.configHelper = configHelper;
        this.timeProvider = timeProvider;
        configHelper.registerAlias(ClusterConfig.class);
        configHelper.allowTypesByWildcard("com.dgphoenix.casino.cassandra.config.**", "com.abs.casino.cassandra.config.**");
    }

    public void load() {
        clusterConfig = (ClusterConfig) configHelper.getConfig(filename);
        checkNotNull(clusterConfig, "Unparsable config file: %s. File may not exists", filename);
        checkState(isNotBlank(clusterConfig.getKeySpace()), "Config must contain keyspace name: %s", filename);
        checkState(!clusterConfig.getParsedHosts().isEmpty(), "Config contains unparsable host list: %s", clusterConfig.getHosts());
        readConsistencyLevel = com.datastax.driver.core.ConsistencyLevel.valueOf(clusterConfig.getReadConsistencyLevel());
        writeConsistencyLevel = com.datastax.driver.core.ConsistencyLevel.valueOf(clusterConfig.getWriteConsistencyLevel());
        if (clusterConfig.getSerialConsistencyLevel() != null) {
            serialConsistencyLevel = com.datastax.driver.core.ConsistencyLevel.valueOf(clusterConfig.getSerialConsistencyLevel());
            checkState(serialConsistencyLevel.isSerial(), "Keyspace serial consistency level can be only SERIAL or LOCAL_SERIAL not %s", serialConsistencyLevel);
        }
    }

    public List<ColumnFamilyConfig> getPersistersConfigs() {
        return clusterConfig.getColumnFamilyConfigs().stream()
                .filter(ColumnFamilyConfig::isEnabled)
                .collect(Collectors.toList());
    }

    public com.datastax.driver.core.Cluster buildCluster(com.datastax.driver.core.Cluster.Builder clusterBuilder) {
        if (clusterConfig.isValidateClusterName() && isNotBlank(clusterConfig.getClusterName())) {
            clusterBuilder.withClusterName(clusterConfig.getClusterName());
        } else {
            LOG.info("Skipping Cassandra cluster-name validation (clusterName='{}', validateClusterName={})",
                    clusterConfig.getClusterName(), clusterConfig.isValidateClusterName());
        }
        com.datastax.driver.core.QueryOptions options = new com.datastax.driver.core.QueryOptions();
        options.setConsistencyLevel(writeConsistencyLevel);
        clusterBuilder.withQueryOptions(options);
        clusterBuilder.addContactPointsWithPorts(clusterConfig.getParsedHosts());
        clusterBuilder.withTimestampGenerator(new NtpTimeGenerator(timeProvider));
        configureLoadBalancing(clusterBuilder);

        com.datastax.driver.core.SocketOptions socketOptions = new com.datastax.driver.core.SocketOptions();
        socketOptions.setConnectTimeoutMillis(clusterConfig.getConnectTimeoutMillis());
        socketOptions.setReadTimeoutMillis(clusterConfig.getReadTimeoutMillis());
        socketOptions.setTcpNoDelay(clusterConfig.isTcpNoDelay());
        socketOptions.setReuseAddress(clusterConfig.isReuseAddress());
        socketOptions.setKeepAlive(clusterConfig.isKeepAlive());
        clusterBuilder.withSocketOptions(socketOptions);

        com.datastax.driver.core.PoolingOptions poolingOptions = new com.datastax.driver.core.PoolingOptions();
        poolingOptions.setMaxConnectionsPerHost(com.datastax.driver.core.HostDistance.LOCAL, clusterConfig.getMaxConnectionsPerHost());
        poolingOptions.setCoreConnectionsPerHost(com.datastax.driver.core.HostDistance.LOCAL, clusterConfig.getCoreConnectionsPerHost());
        // Must remain above socket read timeout to avoid false connection churn.
        poolingOptions.setHeartbeatIntervalSeconds(clusterConfig.getHeartbeatIntervalSeconds());
        //poolingOptions.setPoolTimeoutMillis(10000);
        poolingOptions.setMaxRequestsPerConnection(com.datastax.driver.core.HostDistance.LOCAL, clusterConfig.getMaxRequestsPerConnection());
        clusterBuilder.withPoolingOptions(poolingOptions);
        return clusterBuilder.build();
    }

    private void configureLoadBalancing(com.datastax.driver.core.Cluster.Builder clusterBuilder) {
        if (!clusterConfig.isEnableDcAwareLoadBalancing()) {
            return;
        }
        String localDc = clusterConfig.getLocalDataCenterName();
        if (!isNotBlank(localDc)) {
            LOG.warn("enableDcAwareLoadBalancing=true but localDataCenterName is empty; keeping default policy");
            return;
        }
        clusterBuilder.withLoadBalancingPolicy(
                new com.datastax.driver.core.policies.TokenAwarePolicy(com.datastax.driver.core.policies.DCAwareRoundRobinPolicy.builder().withLocalDc(localDc).build()));
        LOG.info("Enabled DC-aware Cassandra load balancing for localDataCenterName={}", localDc);
    }

    public com.datastax.driver.core.ConsistencyLevel getReadConsistencyLevel() {
        return readConsistencyLevel;
    }

    public com.datastax.driver.core.ConsistencyLevel getWriteConsistencyLevel() {
        return writeConsistencyLevel;
    }

    public com.datastax.driver.core.ConsistencyLevel getSerialConsistencyLevel() {
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
