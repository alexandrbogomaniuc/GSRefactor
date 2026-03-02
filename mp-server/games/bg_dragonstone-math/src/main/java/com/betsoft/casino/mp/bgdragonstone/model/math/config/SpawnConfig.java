package com.betsoft.casino.mp.bgdragonstone.model.math.config;

import com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType;
import com.betsoft.casino.mp.bgdragonstone.model.math.SpawnData;
import com.betsoft.casino.mp.model.gameconfig.ISpawnConfig;
import com.abs.casino.common.util.RNG;

import java.util.Map;

public class SpawnConfig implements ISpawnConfig {
    private static final SpawnConfig defaultConfig = new SpawnConfigLoader().loadDefaultConfig();
    private int singleEnemiesMax;
    private int swarmEnemiesMax;
    private Map<EnemyType, SpawnData> enemies;
    private Map<Integer, SpawnData> swarms;

    public SpawnConfig(int singleEnemiesMax, int swarmEnemiesMax, Map<EnemyType, SpawnData> enemies,
                       Map<Integer, SpawnData> swarms) {
        this.singleEnemiesMax = singleEnemiesMax;
        this.swarmEnemiesMax = swarmEnemiesMax;
        this.enemies = enemies;
        this.swarms = swarms;
    }

    public SpawnData getSpawnData(EnemyType enemyType) {
        return enemies.get(enemyType);
    }

    public int getSingleEnemiesMax() {
        return singleEnemiesMax;
    }

    public int getSwarmEnemiesMax() {
        return swarmEnemiesMax;
    }

    public Map<EnemyType, SpawnData> getEnemies() {
        return enemies;
    }

    public Map<Integer, SpawnData> getSwarms() {
        return swarms;
    }

    public long getWaitTime(EnemyType enemyType) {
        SpawnData enemyData = enemies.get(enemyType);
        return RNG.nextInt(enemyData.getMinWaitTime(), enemyData.getMaxWaitTime()) * 1000L;
    }

    public boolean isUnconditionalRespawn(EnemyType enemyType) {
        return enemies.get(enemyType).isUnconditionalRespawn();
    }

    public long getTimeOffset(EnemyType enemyType) {
        SpawnData enemyData = enemies.get(enemyType);
        return RNG.nextInt(enemyData.getMinTimeOffset(), enemyData.getMaxTimeOffset()) * 1000L;
    }

    public long getStayTime(EnemyType enemyType) {
        SpawnData enemyData = enemies.get(enemyType);
        return RNG.nextInt(enemyData.getMinStayTime(), enemyData.getMaxStayTime()) * 1000L;
    }

    public long getSwarmWaitTime(Integer swarmId) {
        SpawnData swarmData = swarms.get(swarmId);
        if (swarmData == null) {
            swarmData = defaultConfig.getSwarms().get(swarmId);
        }
        return RNG.nextInt(swarmData.getMinWaitTime(), swarmData.getMaxWaitTime()) * 1000L;
    }

    public boolean isSwarmUnconditionalRespawn(Integer swarmId) {
        SpawnData swarmData = swarms.get(swarmId);
        if (swarmData == null) {
            swarmData = defaultConfig.getSwarms().get(swarmId);
        }
        return swarmData.isUnconditionalRespawn();
    }

    public long getSwarmTimeOffset(Integer swarmId) {
        SpawnData swarmData = swarms.get(swarmId);
        if (swarmData == null) {
            swarmData = defaultConfig.getSwarms().get(swarmId);
        }
        return RNG.nextInt(swarmData.getMinTimeOffset(), swarmData.getMaxTimeOffset()) * 1000L;
    }
}
