package com.dgphoenix.casino.web.system.diagnosis.tasks;

import com.datastax.driver.core.Host;
import com.datastax.driver.core.Session;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.IKeyspaceManager;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.web.diagnostic.CheckTask;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class CassandraStateMonitoringTask extends CheckTask {
    private static final Logger LOG = LogManager.getLogger(CassandraStateMonitoringTask.class);
    private static final int LIMIT_WARNING_PENDING_POST_FLUSH = 100;
    private static final int LIMIT_ERROR_PENDING_POST_FLUSH = 1000;
    private static final int LIMIT_WARNING_PENDING_COMPACTION = 200;
    private static final int LIMIT_ERROR_PENDING_COMPACTION = 500;
    private static final String LIMIT_WARNING_USED_SPACE_PROP = "CASSANDRA_WARNING_AT_USED_SPACE_IN_MB";
    private static final String LIMIT_ERROR_USED_SPACE_PROP = "CASSANDRA_ERROR_AT_USED_SPACE_IN_MB";
    private static final String URL_PATTERN = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";

    private final Long warningUsedSpaceInMb;
    private final Long errorUsedSpaceInMb;

    public CassandraStateMonitoringTask() {
        super(null, true);
        GameServerConfiguration cfg = GameServerConfiguration.getInstance();
        warningUsedSpaceInMb = parseSilentlyLongProp(cfg.getStringPropertySilent(LIMIT_WARNING_USED_SPACE_PROP));
        errorUsedSpaceInMb = parseSilentlyLongProp(cfg.getStringPropertySilent(LIMIT_ERROR_USED_SPACE_PROP));
    }

    @Override
    public boolean isOut(boolean strongValidation) {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext().getBean("persistenceManager", CassandraPersistenceManager.class);
        List<HostMetrics> hostMetricsList = persistenceManager.getKeyspaceManagers().stream()
                .flatMap(manager -> resolveJmxHosts(manager).stream())
                .distinct()
                .map(this::getHostMetrics)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (hostMetricsList.isEmpty()) {
            LOG.warn("Cannot obtain host list");
            return false;
        }

        boolean warningFlag = hostMetricsList.stream().anyMatch(getWarningPredicate());
        boolean errorFlag = hostMetricsList.stream().anyMatch(getErrorPredicate());
        String info = hostMetricsList.stream()
                .map(HostMetrics::toString)
                .collect(Collectors.joining("\n"));
        LOG.debug(info);

        if (errorFlag || warningFlag) {
            this.warning = !errorFlag;
            errorMessage = info;
            return true;
        } else {
            return false;
        }
    }

    private Set<String> resolveJmxHosts(IKeyspaceManager manager) {
        Set<String> configuredJmxHosts = manager.getJmxHosts();
        if (configuredJmxHosts != null && !configuredJmxHosts.isEmpty()) {
            return configuredJmxHosts;
        }

        Set<String> fallbackHosts = new LinkedHashSet<>();
        try {
            Session session = manager.getSession();
            if (session != null && session.getCluster() != null && session.getCluster().getMetadata() != null) {
                for (Host host : session.getCluster().getMetadata().getAllHosts()) {
                    String hostAddress = extractHostAddress(host);
                    if (!StringUtils.isBlank(hostAddress)) {
                        fallbackHosts.add(hostAddress);
                    }
                }
            }
        } catch (Throwable e) {
            LOG.debug("Failed to resolve Cassandra JMX hosts from driver metadata for keyspace {}", manager.getKeyspaceName(), e);
        }

        if (!fallbackHosts.isEmpty()) {
            LOG.debug("Using Cassandra JMX host fallback from driver metadata for keyspace {}: {}", manager.getKeyspaceName(), fallbackHosts);
        }
        return fallbackHosts;
    }

    private String extractHostAddress(Host host) {
        if (host == null) {
            return null;
        }
        SocketAddress socketAddress = host.getSocketAddress();
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            String hostString = inetSocketAddress.getHostString();
            if (!StringUtils.isBlank(hostString)) {
                return hostString;
            }
            if (inetSocketAddress.getAddress() != null) {
                return inetSocketAddress.getAddress().getHostAddress();
            }
        }
        return host.getAddress() != null ? host.getAddress().getHostAddress() : null;
    }

    private HostMetrics getHostMetrics(String hostAddress) {
        LOG.debug("Processing host " + hostAddress);
        JMXConnector jmxConnection = null;
        try {
            JMXServiceURL url = new JMXServiceURL(String.format(URL_PATTERN, hostAddress));
            jmxConnection = JMXConnectorFactory.connect(url, null);
            MBeanServerConnection mbsc = jmxConnection.getMBeanServerConnection();
            long pendingCompactionOps = getPendingCompactionOps(mbsc);
            long pendingFlushOps = getPendingFlushMemtableOps(mbsc);
            String opMode = getOperationMode(mbsc);
            long usedSpaceInMb = getUsedSpace(mbsc);
            return new HostMetrics(hostAddress, pendingFlushOps, pendingCompactionOps, opMode, usedSpaceInMb);
        } catch (IOException e) {
            LOG.debug("Failed to connect to cassandra JMX host {} (JMX may be disabled): {}", hostAddress, e.getMessage());
        } finally {
            if (jmxConnection != null) {
                try {
                    jmxConnection.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    private long getPendingCompactionOps(MBeanServerConnection mbsc) {
        try {
            return getLongAttribute(mbsc, "org.apache.cassandra.metrics:type=Compaction,name=PendingTasks", "Value");
        } catch (Exception e) {
            LOG.error("Failed to retrieve pending compaction operation count", e);
            return -1;
        }
    }

    private long getPendingFlushMemtableOps(MBeanServerConnection mbsc) {
        try {
            return getLongAttribute(mbsc, "org.apache.cassandra.metrics:type=ThreadPools,path=internal,scope=MemtablePostFlush,name=PendingTasks", "Value");
        } catch (Exception e) {
            LOG.error("Failed to retrieve pending memtable flush operation count", e);
            return -1;
        }
    }

    private String getOperationMode(MBeanServerConnection mbsc) {
        try {
            return getStringAttribute(mbsc, "org.apache.cassandra.db:type=StorageService", "OperationMode");
        } catch (Exception e) {
            LOG.error("Failed to retrieve current operation mode", e);
            return "unknown";
        }
    }

    private long getUsedSpace(MBeanServerConnection mbsc) {
        try {
            Long spaceUsedInBytes = getLongAttribute(mbsc, "org.apache.cassandra.metrics:type=Storage,name=Load", "Count");
            return spaceUsedInBytes / 1048576;
        } catch (Exception e) {
            LOG.error("Failed to retrieve used space size", e);
            return -1;
        }
    }

    private Long parseSilentlyLongProp(String property) {
        if (!StringUtils.isBlank(property)) {
            try {
                return Long.parseLong(property);
            } catch (NumberFormatException e) {
                LOG.error("Failed to parse integer property " + property);
            }
        }
        return null;
    }

    private Long getLongAttribute(MBeanServerConnection mbsc, String objName, String attrName) throws Exception {
        return Long.parseLong(getAttribute(mbsc, objName, attrName).toString());
    }

    private String getStringAttribute(MBeanServerConnection mbsc, String objName, String attrName) throws Exception {
        return getAttribute(mbsc, objName, attrName).toString();
    }

    private Object getAttribute(MBeanServerConnection mbsc, String objName, String attrName) throws Exception {
        ObjectName objectName = getObjectName(mbsc, objName);
        return mbsc.getAttribute(objectName, attrName);
    }

    private ObjectName getObjectName(MBeanServerConnection mbsc, String name) throws Exception {
        ObjectName query = new ObjectName(name);
        Set<ObjectName> objectNameSet = mbsc.queryNames(query, null);
        return objectNameSet.toArray(new ObjectName[objectNameSet.size()])[0];
    }

    private Predicate<HostMetrics> getWarningPredicate() {
        return host -> host.getPendingCompactionOps() >= LIMIT_WARNING_PENDING_COMPACTION
                || host.getPendingFlushMemtableOps() >= LIMIT_WARNING_PENDING_POST_FLUSH
                || !"NORMAL".equalsIgnoreCase(host.getOperationMode())
                || (warningUsedSpaceInMb != null && warningUsedSpaceInMb.compareTo(host.getSpaceUsedInMb()) <= 0);
    }

    private Predicate<HostMetrics> getErrorPredicate() {
        return host -> host.getPendingCompactionOps() >= LIMIT_ERROR_PENDING_COMPACTION
                || host.getPendingFlushMemtableOps() >= LIMIT_ERROR_PENDING_POST_FLUSH
                || (errorUsedSpaceInMb != null && errorUsedSpaceInMb.compareTo(host.getSpaceUsedInMb()) <= 0);
    }

    private class HostMetrics {
        private final String host;
        private final long pendingFlushMemtableOps;
        private final long pendingCompactionOps;
        private final String operationMode;
        private final long spaceUsedInMb;

        public HostMetrics(String host, long pendingFlushMemtableOps, long pendingCompactionOps, String operationMode,
                           long spaceUsedInMb) {
            this.host = host;
            this.pendingFlushMemtableOps = pendingFlushMemtableOps;
            this.pendingCompactionOps = pendingCompactionOps;
            this.operationMode = operationMode;
            this.spaceUsedInMb = spaceUsedInMb;
        }

        public String getHost() {
            return host;
        }

        public long getPendingFlushMemtableOps() {
            return pendingFlushMemtableOps;
        }

        public long getPendingCompactionOps() {
            return pendingCompactionOps;
        }

        public String getOperationMode() {
            return operationMode;
        }

        public long getSpaceUsedInMb() {
            return spaceUsedInMb;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Cassandra's monitoring parameters for node: ");
            sb.append(host);
            sb.append("\npending compaction operation count: ")
                    .append(pendingCompactionOps)
                    .append(" (warning at: ").append(LIMIT_WARNING_PENDING_COMPACTION)
                    .append(", error at: ").append(LIMIT_ERROR_PENDING_COMPACTION);
            sb.append(")\npending memtable flush operation count: ")
                    .append(pendingFlushMemtableOps)
                    .append(" (warning at: ").append(LIMIT_WARNING_PENDING_POST_FLUSH)
                    .append(", error at: ").append(LIMIT_ERROR_PENDING_POST_FLUSH);
            sb.append(")\nspace used (MB): ")
                    .append(spaceUsedInMb)
                    .append(" (warning at: ").append(warningUsedSpaceInMb)
                    .append(", error at: ").append(errorUsedSpaceInMb);
            sb.append(")\noperation mode: ")
                    .append(operationMode)
                    .append(" (warning when not NORMAL)\n");
            return sb.toString();
        }
    }
}
