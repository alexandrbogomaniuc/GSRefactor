package com.abs.casino.cassandra;

import com.abs.casino.cassandra.config.ClusterConfig;
import com.abs.casino.cassandra.config.ColumnFamilyConfig;
import com.abs.casino.common.configuration.ConfigHelper;
import com.abs.casino.common.util.NtpTimeProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.09.16
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyspaceConfigurationTest {

    private static final String CONFIG_FILENAME = "TestConfig.xml";
    private static final String KEYSPACE_NAME = "TestKS";
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private ConfigHelper configHelper;
    @Mock
    private NtpTimeProvider timeProvider;
    @Mock
    private ClusterConfig clusterConfig;
    private final List<InetSocketAddress> parsedHost = Collections.singletonList(new InetSocketAddress("localhost", 9042));

    @Test
    public void testLoadNonExistentConfig() {
        when(configHelper.getConfig(anyString())).thenReturn(null);
        KeyspaceConfiguration configuration = new KeyspaceConfiguration("NonExistentConfig.xml", configHelper,
                timeProvider);
        thrown.expect(NullPointerException.class);
        thrown.expectMessage(startsWith("Unparsable config file"));

        configuration.load();
    }

    @Test
    public void testConfigWithoutKeyspace() {
        when(configHelper.getConfig(anyString())).thenReturn(new ClusterConfig());
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(startsWith("Config must contain keyspace name"));

        KeyspaceConfiguration configuration = new KeyspaceConfiguration(CONFIG_FILENAME, configHelper, timeProvider);
        configuration.load();
    }

    @Test
    public void testConfigWithUnparsableHostList() {
        when(clusterConfig.getKeySpace()).thenReturn(KEYSPACE_NAME);
        when(configHelper.getConfig(anyString())).thenReturn(clusterConfig);
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(startsWith("Config contains unparsable host list"));

        KeyspaceConfiguration configuration = new KeyspaceConfiguration(CONFIG_FILENAME, configHelper, timeProvider);
        configuration.load();
    }

    @Test
    public void testConfigWithWrongReadConsistencyLevel() {
        when(clusterConfig.getKeySpace()).thenReturn(KEYSPACE_NAME);
        when(clusterConfig.getParsedHosts()).thenReturn(parsedHost);
        when(clusterConfig.getReadConsistencyLevel()).thenReturn("wrong");
        when(configHelper.getConfig(anyString())).thenReturn(clusterConfig);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(startsWith("No enum constant"));

        KeyspaceConfiguration configuration = new KeyspaceConfiguration(CONFIG_FILENAME, configHelper, timeProvider);
        configuration.load();
    }

    @Test
    public void testConfigWithWrongWriteConsistencyLevel() {
        when(clusterConfig.getKeySpace()).thenReturn(KEYSPACE_NAME);
        when(clusterConfig.getParsedHosts()).thenReturn(parsedHost);
        when(clusterConfig.getReadConsistencyLevel()).thenReturn(com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE.toString());
        when(clusterConfig.getWriteConsistencyLevel()).thenReturn("wrong");
        when(configHelper.getConfig(anyString())).thenReturn(clusterConfig);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(startsWith("No enum constant"));

        KeyspaceConfiguration configuration = new KeyspaceConfiguration(CONFIG_FILENAME, configHelper, timeProvider);
        configuration.load();
    }

    @Test
    public void testConfigWithWrongSerialConsistencyLevel() {
        when(clusterConfig.getKeySpace()).thenReturn(KEYSPACE_NAME);
        when(clusterConfig.getParsedHosts()).thenReturn(parsedHost);
        when(clusterConfig.getReadConsistencyLevel()).thenReturn(com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE.toString());
        when(clusterConfig.getWriteConsistencyLevel()).thenReturn(com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE.toString());
        when(clusterConfig.getSerialConsistencyLevel()).thenReturn("wrong");
        when(configHelper.getConfig(anyString())).thenReturn(clusterConfig);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(startsWith("No enum constant"));

        KeyspaceConfiguration configuration = new KeyspaceConfiguration(CONFIG_FILENAME, configHelper, timeProvider);
        configuration.load();
    }

    @Test
    public void testConfigWithNonSerialConsistencyLevel() {
        when(clusterConfig.getKeySpace()).thenReturn(KEYSPACE_NAME);
        when(clusterConfig.getParsedHosts()).thenReturn(parsedHost);
        when(clusterConfig.getReadConsistencyLevel()).thenReturn(com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE.toString());
        when(clusterConfig.getWriteConsistencyLevel()).thenReturn(com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE.toString());
        when(clusterConfig.getSerialConsistencyLevel()).thenReturn(com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE.toString());
        when(configHelper.getConfig(anyString())).thenReturn(clusterConfig);
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(startsWith("Keyspace serial consistency level can be only SERIAL or LOCAL_SERIAL"));

        KeyspaceConfiguration configuration = new KeyspaceConfiguration(CONFIG_FILENAME, configHelper, timeProvider);
        configuration.load();
    }

    @Test
    public void testGetEnabledPersistersConfigs() {
        initMocks();
        List<ColumnFamilyConfig> persistersConfig = Arrays.asList(
                getColumnConfig(true),
                getColumnConfig(false),
                getColumnConfig(true)
        );
        when(clusterConfig.getColumnFamilyConfigs()).thenReturn(persistersConfig);

        KeyspaceConfiguration configuration = new KeyspaceConfiguration(CONFIG_FILENAME, configHelper, timeProvider);
        configuration.load();
        List<ColumnFamilyConfig> actualPersistersConfigs = configuration.getPersistersConfigs();

        assertEquals("Should contains configs only for enabled persisters", 2, actualPersistersConfigs.size());
    }

    private void initMocks() {
        when(clusterConfig.getKeySpace()).thenReturn(KEYSPACE_NAME);
        when(clusterConfig.getParsedHosts()).thenReturn(parsedHost);
        when(clusterConfig.getReadConsistencyLevel()).thenReturn(com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE.toString());
        when(clusterConfig.getWriteConsistencyLevel()).thenReturn(com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE.toString());
        when(configHelper.getConfig(anyString())).thenReturn(clusterConfig);
    }

    private ColumnFamilyConfig getColumnConfig(Boolean isEnabled) {
        ColumnFamilyConfig columnFamilyConfig = mock(ColumnFamilyConfig.class);
        when(columnFamilyConfig.isEnabled()).thenReturn(isEnabled);
        return columnFamilyConfig;
    }
}
