package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IEnemy;
import com.betsoft.casino.mp.model.IEnemyType;
import com.betsoft.casino.mp.model.IExperience;

public class AchievementHelper {

    public static int composeEnemyKey(IEnemy enemy) {
        return composeEnemyKey(enemy.getEnemyClass().getEnemyType(), enemy.getSkin());
    }

    public static int composeEnemyKey(IEnemyType enemyType, int skinId) {
        return enemyType.getId() * enemyType.getMaxSkins() + skinId;
    }

    public static int getPlayerLevel(IExperience xp) {
        return (int) Math.floor((50 + Math.sqrt(2500 + 20 * xp.getAmount())) / 100);
    }

    public static int getPlayerLevel(double xp) {
        return (int) Math.floor((50 + Math.sqrt(2500 + 20 * xp)) / 100);
    }

    public static long getXP(long level) {
        return 500 * (level * level - level);
    }
}
