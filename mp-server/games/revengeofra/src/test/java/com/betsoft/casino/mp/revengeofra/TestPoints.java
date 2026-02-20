package com.betsoft.casino.mp.revengeofra;

import com.betsoft.casino.mp.common.GameMapMeta;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.common.scenarios.SpawnGroup;
import com.betsoft.casino.mp.common.scenarios.SpawnScenario;
import com.betsoft.casino.mp.model.movement.Point;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestPoints {
    private static GameMapStore store = new GameMapStore();
    private static PathMatchingResourcePatternResolver resolver;

    public static void main(String[] args) throws IOException {
        ClassLoader classLoader = TestPoints.class.getClassLoader();
        resolver = new PathMatchingResourcePatternResolver(classLoader);
        GameMapMeta meta = store.loadMeta(resolver.getResource("classpath:maps/revengeofra/502.json"));
        List<SpawnScenario> scenarios = meta.getScenarios();
        SpawnScenario spawnScenario = scenarios.get(0);
        SpawnGroup spawnGroup = spawnScenario.getGroups().get(0);
    }

}
