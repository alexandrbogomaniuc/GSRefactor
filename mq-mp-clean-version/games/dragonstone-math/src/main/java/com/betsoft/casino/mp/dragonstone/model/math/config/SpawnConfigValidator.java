package com.betsoft.casino.mp.dragonstone.model.math.config;

import com.betsoft.casino.mp.dragonstone.model.math.EnemyType;
import com.betsoft.casino.mp.dragonstone.model.math.SpawnData;

import static com.betsoft.casino.mp.dragonstone.model.math.EnemyRange.*;

public class SpawnConfigValidator {

    public String validate(SpawnConfig spawnConfig) {

        if (spawnConfig.getSingleEnemiesMax() < 0) {
            return "Invalid singleEnemiesMax";
        }

        if (spawnConfig.getSwarmEnemiesMax() < 0) {
            return "Invalid swarmEnemiesMax";
        }

        for (EnemyType enemyType : BASE_ENEMIES.getEnemies()) {
            SpawnData data = spawnConfig.getSpawnData(enemyType);
            if (LARGE_ENEMIES.contains(enemyType) || SPECTERS.contains(enemyType) || WIZARDS.contains(enemyType)
                    || enemyType.equals(EnemyType.GARGOYLE)) {
                if (data.getMinTimeOffset() < 0) {
                    return "Invalid min time offset for enemy " + enemyType.name();
                }
                if (data.getMaxTimeOffset() < 0) {
                    return "Invalid max time offset for enemy " + enemyType.name();
                }
                if (data.getMinTimeOffset() > data.getMaxTimeOffset()) {
                    return "Min time offset greater than max time offset for enemy " + enemyType.name();
                }
                if (data.getMaxWaitTime() < 0) {
                    return "Invalid max wait time for enemy " + enemyType.name();
                }
                if (data.getMinWaitTime() < 0) {
                    return "Invalid min wait time for enemy " + enemyType.name();
                }
                if (data.getMinWaitTime() > data.getMaxWaitTime()) {
                    return "Min wait time greater than max wait time for enemy " + enemyType.name();
                }
                if (data.getMaxStayTime() < 0) {
                    return "Invalid max wait time for enemy " + enemyType.name();
                }
                if (data.getMinStayTime() < 0) {
                    return "Invalid min wait time for enemy " + enemyType.name();
                }
                if (data.getMinStayTime() > data.getMaxStayTime()) {
                    return "Min stay time greater than max stay time for enemy " + enemyType.name();
                }
            }
        }

        if (spawnConfig.getSwarms() == null) {
            return "Missed swarms";
        }

        return "";
    }
}