package com.abs.casino.cassandra;

import com.abs.casino.cassandra.config.ClusterConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 26.09.16
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyspaceManagerFactoryTest {

    @Mock
    private KeyspaceConfigurationFactory configurationFactory;
    @Mock
    private KeyspaceConfiguration configuration;
    @Mock
    private ClusterConfig clusterConfig;
    @Mock
    private PersisterDependencyInjector persisterDependencyInjector;

    @Test
    public void testCreateKeyspaceManager() {
        when(configurationFactory.create(anyString())).thenReturn(configuration);
        when(configuration.getClusterConfig()).thenReturn(clusterConfig);

        KeyspaceManagerFactory managerFactory = new KeyspaceManagerFactory(configurationFactory);
        IKeyspaceManager keyspaceManager = managerFactory.create("Config.xml", "SchemaScript.cql", persisterDependencyInjector);

        assertNotNull("Created keyspace manager should be not null", keyspaceManager);
        assertFalse("Keyspace manager should not be initialized after creation", keyspaceManager.isReady());
    }
}
