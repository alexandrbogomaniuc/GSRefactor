package com.betsoft.casino.mp.common;

import org.junit.Before;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class GameMapStoreTest {

    private GameMapStore store = new GameMapStore();
    private PathMatchingResourcePatternResolver resolver;

    @Before
    public void setUp() {
        ClassLoader classLoader = GameMapStoreTest.class.getClassLoader();
        resolver = new PathMatchingResourcePatternResolver(classLoader);
    }

    @Test
    public void getKey() {
        assertEquals(1, store.getKey("1.json"));
        assertEquals(2, store.getKey("2.map"));
    }

    @Test
    public void shouldLoadMeta() throws IOException {
        GameMapMeta meta = store.loadMeta(resolver.getResource("classpath:testMaps/1.json"));
        assertEquals(new GameMapMeta(new PointI(48, 48), new PointD(45.6, 42.3), Collections.singletonMap(2, Arrays.asList(
                new SwarmSpawnParams(92, 40, 6, 6, 4, 52, 4, 4, 3000, 1000, 1000, 1),
                new SwarmSpawnParams(4, 52, 4, 4, 92, 40, 6, 6, 3000, 1000, 1000, 0),
                new SwarmSpawnParams(80, 10, 6, 6, 45, 92, 3, 3, 3000, 1000, 1000, 2.5f),
                new SwarmSpawnParams(45, 92, 3, 3, 80, 10, 6, 6, 3000, 1000, 1000, 3))),
                null, null,null, null, null, null, false, null, null), meta);
    }
}
