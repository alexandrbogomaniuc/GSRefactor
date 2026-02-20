package com.dgphoenix.casino.cassandra.inject;

import com.dgphoenix.casino.cassandra.PersisterDependencyInjector;
import com.dgphoenix.casino.cassandra.persist.engine.ICassandraPersister;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 08.08.2022
 */
public class PersisterDependencyInjectorTest {

    private Map<Class<? extends ICassandraPersister>, ICassandraPersister> persisters;

    private PersisterDependencyInjector dependencyInjector;

    @Before
    public void setUp() throws Exception {
        dependencyInjector = new PersisterDependencyInjector();
        persisters = ImmutableMap.of(
                BravoPersister.class, new BravoPersister(),
                CharliePersister.class, new CharliePersister()
        );
    }

    @Test
    public void collectingDependencies() throws NoSuchMethodException {
        AlphaPersister alphaPersister = new AlphaPersister();
        Map<ICassandraPersister, List<Pair<Class<? extends ICassandraPersister>, Method>>> expected = ImmutableMap.of(
                alphaPersister, Arrays.asList(Pair.of(BravoPersister.class, AlphaPersister.class.getDeclaredMethod("setBravoPersister", BravoPersister.class)),
                        Pair.of(CharliePersister.class, AlphaPersister.class.getDeclaredMethod("setCharliePersister", CharliePersister.class)))
        );

        dependencyInjector.collect(alphaPersister);
        Map<ICassandraPersister, List<Pair<Class<? extends ICassandraPersister>, Method>>> actual = dependencyInjector.getDependencyMap();

        assertEquals(expected, actual);
    }

    @Test
    public void injectingPersisters() throws NoSuchFieldException, IllegalAccessException {
        AlphaPersister alphaPersister = new AlphaPersister();
        dependencyInjector.collect(alphaPersister);

        dependencyInjector.inject(this::getPersister);

        Field bravoPersisterFiled = alphaPersister.getClass().getDeclaredField("bravoPersister");
        bravoPersisterFiled.setAccessible(true);
        BravoPersister bravoPersister = (BravoPersister) bravoPersisterFiled.get(alphaPersister);
        Field charliePersisterFiled = alphaPersister.getClass().getDeclaredField("charliePersister");
        charliePersisterFiled.setAccessible(true);
        CharliePersister charliePersister = (CharliePersister) charliePersisterFiled.get(alphaPersister);

        assertEquals(getPersister(BravoPersister.class), bravoPersister);
        assertEquals(getPersister(CharliePersister.class), charliePersister);
    }

    private ICassandraPersister getPersister(Class<? extends ICassandraPersister> clazz) {
        return persisters.get(clazz);
    }
}
