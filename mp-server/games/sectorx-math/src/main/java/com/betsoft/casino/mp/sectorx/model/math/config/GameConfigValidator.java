package com.betsoft.casino.mp.sectorx.model.math.config;

import com.betsoft.casino.mp.sectorx.model.math.EnemyData;
import com.betsoft.casino.mp.sectorx.model.math.EnemyRange;
import com.betsoft.casino.mp.sectorx.model.math.EnemyType;

import java.util.Map;

public class GameConfigValidator {
    private static final double EPS = 0.0000001;
    private static final int[] weapons = {-1, 7, 9, 10, 3, 4};

    public String validate(GameConfig config) {
        StringBuilder res = new StringBuilder();

        if (config.getGameRTP() <= 0) {
            return "Wrong gameRTP: " + config.getGameRTP();
        }


        Map<Integer, Integer> weaponPrices = config.getWeaponPrices();
        if (weaponPrices == null) {
            return "Missed weaponPrices";
        }


        for (EnemyType enemyType : EnemyRange.BASE_ENEMIES.getEnemies()) {
            EnemyData data = config.getEnemyData(enemyType);
            if (data == null) {
                return "Missed data for enemy " + enemyType.name();
            }
            if (data.getPay() < 0) {
                return "Missed payout for enemy " + enemyType.name();
            }
        }

        Map<Integer, BossParams> bosses = config.getBosses();

        if (bosses == null) {
            return "Missed boss params";
        }

        if (res.length() > 0) {
            return res.toString();
        }

        return "";
    }
}
