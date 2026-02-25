package com.dgphoenix.casino.cassandra;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Snapshot;
import com.datastax.driver.core.*;
import com.dgphoenix.casino.cassandra.config.ClusterConfig;
import com.dgphoenix.casino.cassandra.persist.engine.ICassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 14.09.16
 */
@RunWith(MockitoJUnitRunner.class)
public class KeySpaceManagerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private KeyspaceConfiguration configuration;
    @Mock
    private Cluster.Builder builder;
    @Mock
    private Cluster cluster;
    @Mock
    private Metadata metadata;
    @Mock
    private KeyspaceMetadata keyspaceMetadata;
    @Mock
    private TableMetadata tableMetadata;
    @Mock
    private TableDefinition tableDefinition;
    @Mock
    private Session session;
    @Mock
    private PersistersFactory persistersFactory;
    @Mock
    private ClusterConfig clusterConfig;
    @Mock
    private ICassandraPersister persister;
    private KeyspaceManagerImpl keySpaceManager;

    @Before
    public void setUp() {
        when(cluster.getMetadata()).thenReturn(metadata);
        when(cluster.connect()).thenReturn(session);
        when(configuration.buildCluster(any(Cluster.Builder.class))).thenReturn(cluster);

        when(clusterConfig.getKeySpace()).thenReturn("TestKS");
        when(clusterConfig.getReplicationStrategyClass()).thenReturn("org.apache.cassandra.locator.SimpleStrategy");
        when(clusterConfig.getReplicationFactor()).thenReturn(1);
        when(configuration.getClusterConfig()).thenReturn(clusterConfig);
        when(persistersFactory.getAllPersisters()).thenReturn(Collections.singletonList(persister));

        keySpaceManager = new KeyspaceManagerImpl(configuration, persistersFactory, "cluster.cql");
    }

    @Test
    public void testCreateWithEmptyConfiguration() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Configuration should be specified");

        new KeyspaceManagerImpl(null, null, null);
    }

    @Test
    public void testCreateWithEmptyPersistersFactory() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Persistence factory should be specified");

        new KeyspaceManagerImpl(configuration, null, null);
    }

    @Test
    public void testInitializationWithSchemaCreation() {
        when(configuration.isCreateSchema()).thenReturn(true);
        when(configuration.buildCluster(any(Cluster.Builder.class))).thenReturn(cluster);
        when(cluster.connect(null)).thenReturn(session);
        when(persister.getAllTableDefinitions()).thenReturn(Collections.singletonList(tableDefinition));

        keySpaceManager.init();

        verify(persister).createTable(session, tableDefinition);
        assertTrue("Keyspace manager must be ready after init", keySpaceManager.isReady());
    }

    @Test
    public void testInitializationWithCreateTable() {
        when(configuration.buildCluster(any(Cluster.Builder.class))).thenReturn(cluster);
        when(cluster.connect(null)).thenReturn(session);
        when(metadata.getKeyspace(null)).thenReturn(keyspaceMetadata);
        when(persister.getAllTableDefinitions()).thenReturn(Collections.singletonList(tableDefinition));

        keySpaceManager.init();

        verify(persister).createTable(session, tableDefinition);
        assertTrue("Keyspace manager must be ready after init", keySpaceManager.isReady());
    }

    @Test
    public void testInitializationWithSchemaUpdate() {
        when(configuration.buildCluster(any(Cluster.Builder.class))).thenReturn(cluster);
        when(cluster.connect(null)).thenReturn(session);
        when(metadata.getKeyspace(null)).thenReturn(keyspaceMetadata);
        when(keyspaceMetadata.getTable(null)).thenReturn(tableMetadata);
        when(persister.getAllTableDefinitions()).thenReturn(Collections.singletonList(tableDefinition));

        keySpaceManager.init();

        verify(persister).updateTable(session, tableDefinition, tableMetadata);
        assertTrue("Keyspace manager must be ready after init", keySpaceManager.isReady());
    }

    @Test
    public void testInitializationWithoutSchema() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Cassandra schema not found and schema creation disabled");

        keySpaceManager.init();
    }

    @Test
    public void testShutdown() {
        when(configuration.isCreateSchema()).thenReturn(true);
        when(configuration.buildCluster(any(Cluster.Builder.class))).thenReturn(cluster);
        when(cluster.connect(null)).thenReturn(session);

        keySpaceManager.init();
        assertTrue("Keyspace manager must be ready after init", keySpaceManager.isReady());

        keySpaceManager.shutdown();
        assertFalse("Keyspace manager must be not ready after shutdown", keySpaceManager.isReady());
    }

    @Test(timeout = 5000)
    public void testAwaitOnlineHosts() {
        when(configuration.isCreateSchema()).thenReturn(true);
        when(configuration.getMinimumOnlineHosts()).thenReturn(2L);
        Host firstHost = mock(Host.class);
        when(firstHost.isUp()).thenReturn(true);
        Host secondHost = mock(Host.class);
        when(secondHost.isUp()).thenReturn(true);
        Set<Host> hosts = ImmutableSet.<Host>builder().add(firstHost, secondHost).build();
        when(metadata.getAllHosts()).thenReturn(hosts);
        when(configuration.buildCluster(any(Cluster.Builder.class))).thenReturn(cluster);
        when(cluster.connect(null)).thenReturn(session);

        keySpaceManager.init();
        assertTrue("Keyspace manager must be ready after init", keySpaceManager.isReady());
    }

    @Test(timeout = 5000)
    public void testAwaitOnlineHostsWithSpecificDataCenter() {
        when(configuration.isCreateSchema()).thenReturn(true);
        when(configuration.getMinimumOnlineHosts()).thenReturn(2L);
        when(configuration.getLocalDataCenterName()).thenReturn("dc2");
        Host firstHost = mock(Host.class);
        when(firstHost.getDatacenter()).thenReturn("dc1");
        Host secondHost = mock(Host.class);
        when(secondHost.getDatacenter()).thenReturn("dc2");
        when(secondHost.isUp()).thenReturn(true);
        Host thirdHost = mock(Host.class);
        when(thirdHost.getDatacenter()).thenReturn("dc2");
        when(thirdHost.isUp()).thenReturn(true);
        Set<Host> hosts = ImmutableSet.<Host>builder().add(firstHost, secondHost, thirdHost).build();
        when(metadata.getAllHosts()).thenReturn(hosts);
        when(configuration.buildCluster(any(Cluster.Builder.class))).thenReturn(cluster);
        when(cluster.connect(null)).thenReturn(session);

        keySpaceManager.init();
        assertTrue("Keyspace manager must be ready after init", keySpaceManager.isReady());
    }

    @Test
    public void testDownHosts() {
        when(configuration.isCreateSchema()).thenReturn(true);
        Host firstHost = mock(Host.class);
        when(firstHost.isUp()).thenReturn(true);
        Host secondHost = mock(Host.class);
        when(secondHost.isUp()).thenReturn(false);
        Host thirdHost = mock(Host.class);
        when(thirdHost.isUp()).thenReturn(false);
        Set<Host> hosts = ImmutableSet.<Host>builder().add(firstHost, secondHost, thirdHost).build();
        when(metadata.getAllHosts()).thenReturn(hosts);

        keySpaceManager.init();
        Set<Host> downHosts = keySpaceManager.getDownHosts();

        assertEquals("Wrong size of down hosts set", 2, downHosts.size());
    }

    @Test
    public void testDownHostAddresses() {
        when(configuration.isCreateSchema()).thenReturn(true);
        Host firstHost = mock(Host.class);
        when(firstHost.isUp()).thenReturn(true);

        Host secondHost = mock(Host.class);
        when(secondHost.isUp()).thenReturn(false);
        when(secondHost.getSocketAddress()).thenReturn(new InetSocketAddress("cassandra-down-1", 9042));

        Host thirdHost = mock(Host.class);
        when(thirdHost.isUp()).thenReturn(false);
        when(thirdHost.getSocketAddress()).thenReturn(new InetSocketAddress("cassandra-down-2", 9042));

        Set<Host> hosts = ImmutableSet.<Host>builder().add(firstHost, secondHost, thirdHost).build();
        when(metadata.getAllHosts()).thenReturn(hosts);

        keySpaceManager.init();
        Set<String> downHosts = keySpaceManager.getDownHostAddresses();

        assertEquals("Wrong size of down hosts set", 2, downHosts.size());
        assertTrue("Missing host cassandra-down-1", downHosts.contains("cassandra-down-1"));
        assertTrue("Missing host cassandra-down-2", downHosts.contains("cassandra-down-2"));
    }

    @Test
    public void testAllHostAddresses() {
        when(configuration.isCreateSchema()).thenReturn(true);
        Host firstHost = mock(Host.class);
        when(firstHost.getSocketAddress()).thenReturn(new InetSocketAddress("cassandra-up", 9042));

        Host secondHost = mock(Host.class);
        when(secondHost.getSocketAddress()).thenReturn(new InetSocketAddress("cassandra-down-1", 9042));

        Set<Host> hosts = ImmutableSet.<Host>builder().add(firstHost, secondHost).build();
        when(metadata.getAllHosts()).thenReturn(hosts);

        keySpaceManager.init();
        Set<String> allHosts = keySpaceManager.getAllHostAddresses();

        assertEquals("Wrong size of host set", 2, allHosts.size());
        assertTrue("Missing host cassandra-up", allHosts.contains("cassandra-up"));
        assertTrue("Missing host cassandra-down-1", allHosts.contains("cassandra-down-1"));
    }

    @Test
    public void testMetricsSnapshotUnavailableBeforeInit() {
        KeyspaceMetricsSnapshot snapshot = keySpaceManager.getMetricsSnapshot();
        assertNotNull("Snapshot should not be null", snapshot);
        assertFalse("Snapshot should be unavailable before manager init", snapshot.isAvailable());
    }

    @Test
    public void testMetricsSnapshotAfterInit() {
        when(configuration.isCreateSchema()).thenReturn(true);
        Metrics metrics = mock(Metrics.class);
        Metrics.Errors errorMetrics = mock(Metrics.Errors.class);
        com.codahale.metrics.Timer requestsTimer = mock(com.codahale.metrics.Timer.class);
        Snapshot latencySnapshot = mock(Snapshot.class);

        when(cluster.getMetrics()).thenReturn(metrics);
        when(metrics.getKnownHosts()).thenReturn(() -> 2);
        when(metrics.getConnectedToHosts()).thenReturn(() -> 1);
        when(metrics.getOpenConnections()).thenReturn(() -> 3);
        when(metrics.getTrashedConnections()).thenReturn(() -> 0);
        when(metrics.getBlockingExecutorQueueDepth()).thenReturn(() -> 0);
        when(metrics.getExecutorQueueDepth()).thenReturn(() -> 1);
        when(metrics.getRequestsTimer()).thenReturn(requestsTimer);
        when(requestsTimer.getSnapshot()).thenReturn(latencySnapshot);
        when(requestsTimer.getCount()).thenReturn(9L);
        when(requestsTimer.getMeanRate()).thenReturn(1.5d);
        when(requestsTimer.getOneMinuteRate()).thenReturn(1.1d);
        when(requestsTimer.getFiveMinuteRate()).thenReturn(1.2d);
        when(requestsTimer.getFifteenMinuteRate()).thenReturn(1.3d);
        when(latencySnapshot.getMin()).thenReturn(10L);
        when(latencySnapshot.getMax()).thenReturn(20L);
        when(latencySnapshot.getMean()).thenReturn(15d);
        when(latencySnapshot.getMedian()).thenReturn(14d);
        when(latencySnapshot.getStdDev()).thenReturn(2d);
        when(metrics.getErrorMetrics()).thenReturn(errorMetrics);
        when(errorMetrics.getConnectionErrors()).thenReturn(counter(4L));
        when(errorMetrics.getIgnores()).thenReturn(counter(0L));
        when(errorMetrics.getIgnoresOnReadTimeout()).thenReturn(counter(0L));
        when(errorMetrics.getIgnoresOnWriteTimeout()).thenReturn(counter(0L));
        when(errorMetrics.getIgnoresOnUnavailable()).thenReturn(counter(0L));
        when(errorMetrics.getRetries()).thenReturn(counter(1L));
        when(errorMetrics.getRetriesOnReadTimeout()).thenReturn(counter(0L));
        when(errorMetrics.getRetriesOnWriteTimeout()).thenReturn(counter(0L));
        when(errorMetrics.getRetriesOnUnavailable()).thenReturn(counter(0L));
        when(errorMetrics.getReadTimeouts()).thenReturn(counter(0L));
        when(errorMetrics.getWriteTimeouts()).thenReturn(counter(0L));
        when(errorMetrics.getUnavailables()).thenReturn(counter(0L));
        when(errorMetrics.getOthers()).thenReturn(counter(0L));
        when(errorMetrics.getSpeculativeExecutions()).thenReturn(counter(0L));

        keySpaceManager.init();

        KeyspaceMetricsSnapshot snapshot = keySpaceManager.getMetricsSnapshot();
        assertNotNull("Snapshot should not be null after init", snapshot);
        assertTrue("Snapshot should be available after init", snapshot.isAvailable());
        assertEquals("Wrong known hosts value", 2L, snapshot.getKnownHosts());
        assertEquals("Wrong requests count", 9L, snapshot.getRequestsCount());
        assertEquals("Wrong connection errors", 4L, snapshot.getConnectionErrors());
    }

    private Counter counter(long value) {
        Counter counter = new Counter();
        counter.inc(value);
        return counter;
    }
}
