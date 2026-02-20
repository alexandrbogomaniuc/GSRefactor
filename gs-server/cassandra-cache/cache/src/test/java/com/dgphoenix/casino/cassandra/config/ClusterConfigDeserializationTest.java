package com.dgphoenix.casino.cassandra.config;

import com.google.common.collect.ImmutableMap;
import com.thoughtworks.xstream.XStream;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by isador
 * on 20.06.17
 */
public class ClusterConfigDeserializationTest {

    private XStream xStream;

    @Before
    public void setUp() throws Exception {
        xStream = new XStream();
        XStream.setupDefaultSecurity(xStream);
        xStream.allowTypes(new Class[]{
                ClusterConfig.class,
                ColumnFamilyConfig.class,
                Host.class
        });
        xStream.processAnnotations(ClusterConfig.class);
    }

    @Test
    public void testSimpleStrategy() {
        ClusterConfig config = (ClusterConfig) xStream.fromXML(getFile("SimpleStrategyClusterConfig.xml"));

        Set<String> expectedJmxHostSet = new HashSet<>();
        expectedJmxHostSet.add("c1:7199");
        expectedJmxHostSet.add("c2:7299");

        assertNotNull("Config must be not null", config);
        assertEquals("Actual clusterName doesn't equals", "clusterName", config.getClusterName());
        assertEquals("Actual keySpaceName doesn't equals", "ksName", config.getKeySpace());
        assertEquals("Actual replicationStrategyClass doesn't equals", "org.apache.cassandra.locator.SimpleStrategy", config.getReplicationStrategyClass());
        assertEquals("Actual readConsistencyLevel doesn't equals", "2", config.getReadConsistencyLevel());
        assertEquals("Actual writeConsistencyLevel doesn't equals", "2", config.getWriteConsistencyLevel());
        assertTrue("Actual create scheme doesn't equals", config.isCreateScheme());
        assertEquals("Actual hosts doesn't equals", "hostsblablalba", config.getHosts());
        assertEquals("Actual jmx hosts doesn't equals", expectedJmxHostSet, config.getJmxHosts());
        assertEquals("Actual minimumOnlineHosts doesn't equals", 3, config.getMinimumOnlineHosts());
        assertNotNull("Actual column config list must be not null", config.getColumnFamilyConfigs());
        assertEquals("Actual replication factor doesn't equals", 1, config.getReplicationFactor());
        assertCFEquals(new ColumnFamilyConfig("com.dgphoenix.casino.alert.AlertPersister", 604800, true), config.getColumnFamilyConfigs().get(0));
        assertCFEquals(new ColumnFamilyConfig("com.dgphoenix.casino.alert.BucketPersister", 604800, true), config.getColumnFamilyConfigs().get(1));
    }

    @Test
    public void testNetworkTopologyStrategy() {
        ClusterConfig config = (ClusterConfig) xStream.fromXML(getFile("NetworkTopologyClusterConfig.xml"));

        assertNotNull("Config must be not null", config);
        assertEquals("Actual clusterName doesn't equals", "clusterName", config.getClusterName());
        assertEquals("Actual keySpaceName doesn't equals", "ksName", config.getKeySpace());
        assertEquals("Actual replicationStrategyClass doesn't equals", "org.apache.cassandra.locator.NetworkTopologyStrategy", config.getReplicationStrategyClass());
        assertEquals("Actual readConsistencyLevel doesn't equals", "2", config.getReadConsistencyLevel());
        assertEquals("Actual writeConsistencyLevel doesn't equals", "2", config.getWriteConsistencyLevel());
        assertEquals("Actual serialConsistencyLevel doesn't equals", "LOCAL_SERIAL", config.getSerialConsistencyLevel());
        assertTrue("Actual create scheme doesn't equals", config.isCreateScheme());
        assertEquals("Actual hosts doesn't equals", "hostsblablalba", config.getHosts());
        assertEquals("Actual minimumOnlineHosts doesn't equals", 3, config.getMinimumOnlineHosts());
        assertEquals("Actual localDataCenterName doesn't equals", "dc02", config.getLocalDataCenterName());
        assertNotNull("Actual column config list must be not null", config.getColumnFamilyConfigs());
        assertCFEquals(new ColumnFamilyConfig("com.dgphoenix.casino.alert.AlertPersister", 604800, true), config.getColumnFamilyConfigs().get(0));
        assertCFEquals(new ColumnFamilyConfig("com.dgphoenix.casino.alert.BucketPersister", 604800, true), config.getColumnFamilyConfigs().get(1));
        assertEquals(getExpectedDCRFConfig().entrySet(), config.getDataCenterReplicationFactor().entrySet());
    }

    private Map<String, String> getExpectedDCRFConfig() {
        return ImmutableMap.of("dc01", "1", "dc02", "3");
    }

    private void assertCFEquals(ColumnFamilyConfig expected, ColumnFamilyConfig actual) {
        assertEquals("Actual cf classname doesn't equals", expected.getClassName(), actual.getClassName());
        assertEquals("Actual cf ttl doesn't equals", expected.getTtl(), actual.getTtl());
        assertEquals("Actual cf enabled doesn't equals", expected.isEnabled(), actual.isEnabled());
    }

    private File getFile(String filename) {
        URL fileUrl = ClusterConfigDeserializationTest.class.getClassLoader().getResource(filename);
        if (fileUrl != null) {
            return new File(fileUrl.getFile());
        }
        throw new RuntimeException("File not found: " + filename);
    }
}
