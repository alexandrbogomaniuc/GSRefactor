package com.abs.casino.cassandra;

import com.abs.casino.cassandra.persist.CassandraPersisterMock;
import com.abs.casino.cassandra.persist.ISimplePersister;
import com.abs.casino.cassandra.persist.SimplePersisterImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 26.09.16
 */
@RunWith(MockitoJUnitRunner.class)
public class CassandraPersistenceManagerTest {

    private static final String KS1 = "KeyspaceName1";
    private static final String KS2 = "KeyspaceName2";
    private static final String KS3 = "KeyspaceName3";

    @Mock
    private KeyspaceManagerFactory managerFactory;
    @Mock
    private IKeyspaceManager keyspaceManagerMock;
    @Mock
    private CassandraPersisterMock persisterMock;
    @Mock
    private SimplePersisterImpl simplePersister;
    @Mock
    private PersisterDependencyInjector persisterDependencyInjector;

    @Test
    public void testGetKeyspaceManager() {
        when(managerFactory.create(anyString(), anyString(), any())).thenReturn(keyspaceManagerMock);
        when(keyspaceManagerMock.getKeyspaceName()).thenReturn(KS1);

        CassandraPersistenceManager persistenceManager = new CassandraPersistenceManager(managerFactory, persisterDependencyInjector);
        persistenceManager.addKeyspace("ConfigKeyspace.xml", "UpdateSchema.cql");
        IKeyspaceManager keyspaceManager = persistenceManager.getKeyspaceManager(KS1);

        assertNull("Nonexistent keyspace should be null", persistenceManager.getKeyspaceManager("NonexistentKeyspace"));
        assertNotNull("Keyspace should be not null", keyspaceManager);
        assertEquals("Keyspace name should be: " + KS1, KS1, keyspaceManager.getKeyspaceName());
    }

    @Test
    public void testGetPersisterFromAllKeyspaces() {
        when(managerFactory.create(anyString(), ArgumentMatchers.<String>isNull(), any())).thenReturn(keyspaceManagerMock);
        when(keyspaceManagerMock.getKeyspaceName()).thenReturn(KS1, KS2, KS3);
        when(keyspaceManagerMock.getPersister(CassandraPersisterMock.class))
                .thenReturn(persisterMock, null, persisterMock);

        CassandraPersistenceManager persistenceManager = new CassandraPersistenceManager(managerFactory, persisterDependencyInjector)
                .addKeyspace("Config1.xml", null)
                .addKeyspace("Config2.xml", null)
                .addKeyspace("Config3.xml", null);
        Map<String, CassandraPersisterMock> persistersByKeyspace = persistenceManager.getPersisters(CassandraPersisterMock.class);

        assertEquals("Persisters map has wrong size", 2, persistersByKeyspace.size());
        assertTrue("Should contains keyspace with name: " + KS1, persistersByKeyspace.containsKey(KS1));
        assertTrue("Should contains keyspace with name: " + KS3, persistersByKeyspace.containsKey(KS3));
    }

    @Test
    public void testGetFirstPersister() {
        when(managerFactory.create(anyString(), ArgumentMatchers.<String>isNull(), any())).thenReturn(keyspaceManagerMock);
        when(keyspaceManagerMock.getKeyspaceName()).thenReturn(KS1, KS2, KS3);
        when(keyspaceManagerMock.getPersister(CassandraPersisterMock.class))
                .thenReturn(null, persisterMock, persisterMock);

        CassandraPersistenceManager persistenceManager = new CassandraPersistenceManager(managerFactory, persisterDependencyInjector)
                .addKeyspace("Config1.xml", null)
                .addKeyspace("Config2.xml", null)
                .addKeyspace("Config3.xml", null);
        CassandraPersisterMock persister = persistenceManager.getPersister(CassandraPersisterMock.class);

        assertNotNull("Persister should be found", persister);
    }

    @Test
    public void testGetPersisterByInterface() {
        when(managerFactory.create(anyString(), ArgumentMatchers.<String>isNull(), any())).thenReturn(keyspaceManagerMock);
        when(keyspaceManagerMock.getKeyspaceName()).thenReturn(KS1, KS2, KS3);
        when(keyspaceManagerMock.getPersistersByInterface(ISimplePersister.class))
                .thenReturn(Arrays.<ISimplePersister>asList(persisterMock, simplePersister));
        CassandraPersistenceManager persistenceManager = new CassandraPersistenceManager(managerFactory, persisterDependencyInjector)
                .addKeyspace("Config1.xml", null)
                .addKeyspace("Config2.xml", null)
                .addKeyspace("Config3.xml", null);

        ISimplePersister persister = persistenceManager.getPersisterByInterface(ISimplePersister.class);

        assertNotNull("Persister should be found", persister);
        assertEquals(persisterMock, persister);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetPersistersListByInterface() {
        when(managerFactory.create(anyString(), ArgumentMatchers.<String>isNull(), any())).thenReturn(keyspaceManagerMock);
        when(keyspaceManagerMock.getKeyspaceName()).thenReturn(KS1, KS2, KS3);
        when(keyspaceManagerMock.getPersistersByInterface(ISimplePersister.class)).thenReturn(
                Arrays.<ISimplePersister>asList(persisterMock, simplePersister),
                Collections.<ISimplePersister>emptyList(),
                Collections.singletonList(mock(ISimplePersister.class))
        );
        CassandraPersistenceManager persistenceManager = new CassandraPersistenceManager(managerFactory, persisterDependencyInjector)
                .addKeyspace("Config1.xml", null)
                .addKeyspace("Config2.xml", null)
                .addKeyspace("Config3.xml", null);

        List<ISimplePersister> persisters = persistenceManager.getPersistersByInterface(ISimplePersister.class);

        assertEquals("Persisters list has wrong size", 3, persisters.size());
        assertEquals(persisterMock, persisters.get(0));
        assertEquals(simplePersister, persisters.get(1));
    }
}
