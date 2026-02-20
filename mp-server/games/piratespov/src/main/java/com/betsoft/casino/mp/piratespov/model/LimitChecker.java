package com.betsoft.casino.mp.piratespov.model;

import com.betsoft.casino.mp.model.IEnemyLimitChecker;
import com.betsoft.casino.mp.piratescommon.model.math.EnemyType;
import java.util.HashMap;
import java.util.Map;
import static com.betsoft.casino.mp.piratescommon.model.math.EnemyType.*;

public class LimitChecker implements IEnemyLimitChecker<Enemy> {

    private static final int RATS_LIMIT = 40;
    private static final int CRABS_LIMIT = 40;
    private static final int TOTAL_LIMIT = 100;
    private static final int HIGH_ENEMIES_LIMIT = 5;
    private static final int MEDIUM_ENEMIES_LIMIT = 10;
    private static final int LEVIATHAN_SKIN_ID = 3;
    private static final int DECKHANDS_LIMIT = 3;
    private static final int RUNNERS_LIMIT = 2;
    private static final int TROLLS_LIMIT = 2;
    private static final int BIRDS_LIMIT = 5;

    private int bossSkin;
    private int totalEnemies;
    private Map<Integer, Integer> aliveEnemies = new HashMap<>();

    @Override
    public void reset() {
        totalEnemies = 0;
        bossSkin = 0;
        aliveEnemies.clear();
    }

    @Override
    public void countEnemy(Enemy enemy) {
        int typeId = enemy.getEnemyClass().getEnemyType().getId();
        aliveEnemies.put(typeId, aliveEnemies.getOrDefault(typeId, 0) + 1);
        totalEnemies++;
        if (enemy.isBoss()) {
            bossSkin = enemy.getSkin();
        }
    }

    public boolean isSpawnAllowed(EnemyType enemyType) {
        if (totalEnemies >= TOTAL_LIMIT) {
            return false;
        }
        switch (enemyType) {
            case ENEMY_1:
            case ENEMY_2:
            case ENEMY_3:
                return isRatsSpawnAllowed();
            case ENEMY_4:
            case ENEMY_5:
            case ENEMY_6:
            case ENEMY_7:
            case ENEMY_8:
                return isCrabsSpawnAllowed();
            case ENEMY_9:
            case ENEMY_10:
                return getCount(enemyType) < BIRDS_LIMIT;
            case ENEMY_15:
            case ENEMY_16:
                return getCount(enemyType) < MEDIUM_ENEMIES_LIMIT;
            case ENEMY_11:
            case ENEMY_12:
                return isDeckhandSpawnAllowed();
            case ENEMY_13:
            case ENEMY_14:
                return isNeckbeardSpawnAllowed();
            case ENEMY_17:
            case ENEMY_18:
                return isRunnerSpawnAllowed();
            case ENEMY_19:
            case ENEMY_20:
                return getCount(EnemyRange.TROLLS) < TROLLS_LIMIT;
            case WEAPON_CARRIER:
                return getCount(EnemyRange.WEAPON_CARRIER) == 0;
            case Boss:
                return isBossAlive();
            default:
                return false;
        }
    }

    public boolean isRatsSpawnAllowed() {
        return getRatsCount() < RATS_LIMIT;
    }

    private int getRatsCount() {
        return getCount(ENEMY_1) + getCount(ENEMY_2) + getCount(ENEMY_3);
    }

    public int getRatsAllowed() {
        return RATS_LIMIT - getRatsCount();
    }

    public boolean isCrabsSpawnAllowed() {
        return getCount(ENEMY_4) + getCount(ENEMY_5) + getCount(ENEMY_6) + getCount(ENEMY_7) + getCount(ENEMY_8) < CRABS_LIMIT;
    }

    private int getCrabsCount() {
        return getCount(ENEMY_4) + getCount(ENEMY_5) + getCount(ENEMY_6) + getCount(ENEMY_7) + getCount(ENEMY_8);
    }

    public int getCrabsAllowed() {
        return CRABS_LIMIT - getCrabsCount();
    }

    public boolean isDeckhandSpawnAllowed() {
        return getCount(ENEMY_11) + getCount(ENEMY_12) < DECKHANDS_LIMIT;
    }

    public boolean isNeckbeardSpawnAllowed() {
        return false;
    }

    public boolean isRunnerSpawnAllowed() {
        return getCount(ENEMY_17) + getCount(ENEMY_18) < RUNNERS_LIMIT;
    }

    public boolean isCaptainsSpawnAllowed() {
        return getCount(EnemyRange.CAPTAINS) < HIGH_ENEMIES_LIMIT;
    }

    public boolean isBossAlive() {
        return bossSkin > 0;
    }

    public boolean isLeviathanAlive() {
        return bossSkin == LEVIATHAN_SKIN_ID;
    }

    public int getCount(EnemyType enemyType) {
        return aliveEnemies.getOrDefault(enemyType.getId(), 0);
    }

    public int getCount(EnemyRange enemyRange) {
        return enemyRange.getEnemies().stream().mapToInt(this::getCount).sum();
    }

    public int getTotalEnemies() {
        return totalEnemies;
    }
}
