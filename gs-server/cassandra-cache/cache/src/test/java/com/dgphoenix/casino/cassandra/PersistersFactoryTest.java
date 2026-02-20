package com.dgphoenix.casino.cassandra;

import com.dgphoenix.casino.cassandra.config.ColumnFamilyConfig;
import com.dgphoenix.casino.cassandra.persist.CassandraPersisterMock;
import com.dgphoenix.casino.cassandra.persist.CassandraRemoteCallPersister;
import com.dgphoenix.casino.cassandra.persist.ISimplePersister;
import com.dgphoenix.casino.cassandra.persist.engine.ICassandraPersister;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.datastax.driver.core.ConsistencyLevel.*;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.09.16
 */
@RunWith(MockitoJUnitRunner.class)
public class PersistersFactoryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private ColumnFamilyConfig cfConfig;
    @Mock
    private PersisterDependencyInjector persisterDependencyInjector;
    private PersistersFactory persistersFactory;

    @Before
    public void setUp() {
        persistersFactory = new PersistersFactory(persisterDependencyInjector);
    }

    @Test
    public void testInitNonExistentPersister() {
        when(cfConfig.getClassName()).thenReturn("com.dgphoenix.casino.cassandra.UnExistentPersister");
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(startsWith("Wrong persister class name"));

        persistersFactory.initializePersisters(Collections.singletonList(cfConfig), ANY, ANY, LOCAL_SERIAL);
    }

    @Test
    public void testInitNotCassandraPersister() {
        when(cfConfig.getClassName()).thenReturn("com.dgphoenix.casino.cassandra.persist.WrongPersister");
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Persister must implement ICassandraPersister");

        persistersFactory.initializePersisters(Collections.singletonList(cfConfig), ANY, ANY, LOCAL_SERIAL);
    }

    @Test
    public void testInitializePersisters() {
        when(cfConfig.getClassName()).thenReturn("com.dgphoenix.casino.cassandra.persist.CassandraPersisterMock");

        persistersFactory.initializePersisters(Collections.singletonList(cfConfig), ANY, ANY, LOCAL_SERIAL);

        List<ICassandraPersister> persisters = persistersFactory.getAllPersisters();
        assertEquals(1, persisters.size());
        assertSame(CassandraPersisterMock.class, persisters.get(0).getClass());
    }

    @Test
    public void testGetPersisterByClass() {
        when(cfConfig.getClassName()).thenReturn("com.dgphoenix.casino.cassandra.persist.CassandraPersisterMock");

        persistersFactory.initializePersisters(Collections.singletonList(cfConfig), ANY, ANY, LOCAL_SERIAL);
        ICassandraPersister persister = persistersFactory.getPersister(CassandraRemoteCallPersister.class);

        assertNull("For class that not in config should returns null", persister);

        persister = persistersFactory.getPersister(CassandraPersisterMock.class);

        assertNotNull("For valid persister class from config should return persister instance", persister);
    }

    @Test
    public void testGetPersisterByInterface() {
        when(cfConfig.getClassName()).thenReturn("com.dgphoenix.casino.cassandra.persist.CassandraPersisterMock");

        persistersFactory.initializePersisters(Collections.singletonList(cfConfig), ANY, ANY, LOCAL_SERIAL);
        List<ISimplePersister> persister = persistersFactory.getPersistersByInterface(ISimplePersister.class);

        assertEquals(1, persister.size());
        assertNotNull("Should be not null if interface is implemented by persister", persister.get(0));
    }

    @Test
    public void testGetPersisterListByInterface() {
        ColumnFamilyConfig config1 = mock(ColumnFamilyConfig.class);
        String persisterClassName1 = "com.dgphoenix.casino.cassandra.persist.CassandraPersisterMock";
        when(config1.getClassName()).thenReturn(persisterClassName1);
        ColumnFamilyConfig config2 = mock(ColumnFamilyConfig.class);
        String persisterClassName2 = "com.dgphoenix.casino.cassandra.persist.SimplePersisterImpl";
        when(config2.getClassName()).thenReturn(persisterClassName2);

        persistersFactory.initializePersisters(Arrays.asList(config1, config2), ANY, ANY, LOCAL_SERIAL);
        List<ISimplePersister> persisters = persistersFactory.getPersistersByInterface(ISimplePersister.class);

        assertEquals(2, persisters.size());
        assertEquals(persisterClassName1, persisters.get(0).getClass().getName());
        assertEquals(persisterClassName2, persisters.get(1).getClass().getName());
    }
}
