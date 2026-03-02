package com.abs.casino.cassandra;

import com.abs.casino.cassandra.config.ClusterConfig;
import com.abs.casino.common.configuration.ConfigHelper;
import com.abs.casino.common.util.NtpTimeProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetSocketAddress;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 26.09.16
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyspaceConfigurationFactoryTest {

    private static final String KEYSPACE_NAME = "TestKS";

    @Mock
    private ConfigHelper configHelper;
    @Mock
    private NtpTimeProvider timeProvider;
    @Mock
    private ClusterConfig clusterConfig;

    @Test
    public void testCreateKeyspaceConfiguration() {
        when(configHelper.getConfig(anyString())).thenReturn(clusterConfig);
        when(clusterConfig.getKeySpace()).thenReturn("TestKS");
        when(clusterConfig.getParsedHosts()).thenReturn(Collections.singletonList(new InetSocketAddress("localhost", 123)));
        when(clusterConfig.getReadConsistencyLevel()).thenReturn(com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE.toString());
        when(clusterConfig.getWriteConsistencyLevel()).thenReturn(com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE.toString());

        KeyspaceConfigurationFactory configurationFactory = new KeyspaceConfigurationFactory(configHelper, timeProvider);
        KeyspaceConfiguration configuration = configurationFactory.create("Config.xml");

        assertNotNull("Created configuration should be not null", configuration);
        assertEquals("Configuration should have keyspace name: " + KEYSPACE_NAME, KEYSPACE_NAME, configuration.getKeyspaceName());
    }
}
