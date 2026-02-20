package com.betsoft.casino.mp.maxblastchampions.model.math;

import com.betsoft.casino.mp.model.IEnemyRange;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static com.betsoft.casino.mp.maxblastchampions.model.math.EnemyType.ROCKET;

public enum EnemyRange implements IEnemyRange<EnemyType> {
    BASE_ENEMIES(Collections.singletonList(ROCKET));

    private List<EnemyType> enemies;

    @Override
    public List<EnemyType> getEnemies() {
        return enemies;
    }

    EnemyRange(List<EnemyType> enemies) {
        this.enemies = enemies;
    }

    public boolean contains(EnemyType enemyType) {
        return getEnemies().contains(enemyType);
    }
}
