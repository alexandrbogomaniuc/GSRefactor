package com.betsoft.casino.mp.sectorx;

import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.sectorx.model.Enemy;
import com.betsoft.casino.mp.sectorx.model.EnemySpecialItem;
import com.betsoft.casino.mp.sectorx.model.GameMap;
import com.betsoft.casino.mp.sectorx.model.math.EnemyType;
import com.betsoft.casino.mp.sectorx.model.math.config.GameConfig;
import com.betsoft.casino.mp.sectorx.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.sectorx.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.sectorx.model.math.config.SpawnConfigLoader;
import com.dgphoenix.casino.common.util.RNG;

public class TestMathItem {
    EnemyType enemyType = EnemyType.F7;
    int payForItem = 70;
    Money stake = Money.fromCents(20);
    int countShots = 1000000;

    public static void main(String[] args) {
        SpawnConfig spawnConfig = new SpawnConfigLoader().loadDefaultConfig();
        GameMap map = new GameMap();
        GameConfig gameConfig = new GameConfigLoader().loadDefaultConfig();

        TestMathItem testMathItem = new TestMathItem();
        testMathItem.doTest(gameConfig, map, spawnConfig);
    }


    private void doTest(GameConfig gameConfig, GameMap map, SpawnConfig spawnConfig) {
        double twin = 0;
        double tbet = 0;

        Enemy enemy = map.addEnemyWithTrajectory(enemyType, map.getRandomTrajectory(enemyType, spawnConfig));
        EnemySpecialItem enemySpecialItem = (EnemySpecialItem) enemy;

        for (int i = 0; i < countShots; i++) {
            double hitProbability = gameConfig.getGameRTP() / 100 / payForItem;
            tbet = tbet + stake.toDoubleCents();
            boolean isHit = RNG.rand() < hitProbability;
            if (isHit) {
                Money payout = stake.getWithMultiplier(payForItem);
                twin = twin + payout.getWithMultiplier(1).toDoubleCents();
            }

            if (i % 1000 == 0) {
                System.out.println("i: " + i);
                double rtp = twin / tbet * 100;
                System.out.println("tbet: " + tbet);
                System.out.println("twin: " + twin);
                System.out.println("rtp: " + rtp);
                System.out.println("_________________________________________________");
            }
        }
        System.out.println("total rtp: " + twin / tbet * 100);
    }
}
