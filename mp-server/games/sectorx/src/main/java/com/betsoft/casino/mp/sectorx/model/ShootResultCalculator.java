package com.betsoft.casino.mp.sectorx.model;

import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.sectorx.model.math.EnemyRange;
import com.betsoft.casino.mp.sectorx.model.math.EnemyType;
import com.betsoft.casino.mp.sectorx.model.math.MathData;
import com.betsoft.casino.mp.sectorx.model.math.config.GameConfig;
import com.betsoft.casino.mp.sectorx.model.math.config.MinMaxParams;
import com.dgphoenix.casino.common.util.RNG;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.betsoft.casino.mp.sectorx.model.math.EnemyType.*;

public class ShootResultCalculator {

    public class Result {
        private final int pay;
        private final boolean applicable;

        public Result(int pay) {
            this.pay = pay;
            this.applicable = true;
        }

        public Result() {
            this.applicable = false;
            this.pay = 0;
        }

        public int getPay() {
            return pay;
        }

        public boolean isApplicable() {
            return applicable;
        }
    }

    private final Result freezeResult;
    private final Result bossRandomPayResult;

    private final Result enemyPay;

    private final PlayGameState playGameState;
    private final Enemy baseEnemy;
    private final Seat seat;
    private final Logger logger;

    public ShootResultCalculator(PlayGameState playGameState, Enemy baseEnemy, Seat seat, Logger logger) {
        this.playGameState = playGameState;
        this.baseEnemy = baseEnemy;
        this.seat = seat;
        this.logger = logger;

        if (baseEnemy == null) {
            this.freezeResult = new Result();
            this.bossRandomPayResult = new Result();
            this.enemyPay = new Result();
            return;
        }

        getLog().debug("Calculating the additional payout for the enemy: {}", baseEnemy.getEnemyType().getName());

        this.freezeResult = calculateFreeze();
        this.bossRandomPayResult = calculateBossRandomPay();

        this.enemyPay = calculateEnemyPay();
    }

    Logger getLog() {
        return logger;
    }

    public Result getEnemyPay() {
        return enemyPay;
    }

    public Result getBossRandomPayResult() {
        return bossRandomPayResult;
    }

    public Result getFreezeResult() {
        return freezeResult;
    }

    public Result calculateFreeze() {
        boolean isFreezeEnemy = baseEnemy != null && baseEnemy.getTrajectory().getPoints().stream().noneMatch(Point::isFreezePoint);
        if (playGameState.getLastFreezeTime() > 0 && isFreezeEnemy) {
            int remainingAdditionalWin = seat.getAdditionalTempCounters(playGameState.SEAT_FREEZE_WIN_AMOUNT);
            if (RNG.nextBoolean() && remainingAdditionalWin > 0) {
                int randomPay = RNG.nextInt(2, 6);
                if (randomPay > remainingAdditionalWin) {
                    randomPay = remainingAdditionalWin;
                }

                getLog().debug("Calculated Freeze Payout: {}, remainingAdditionalWin: {}", randomPay, remainingAdditionalWin);
                return new Result(randomPay);
            }
        }

        return new Result();
    }

    public Result calculateBossRandomPay() {
        EnemyType baseEnemyType = baseEnemy.getEnemyType();
        if (baseEnemyType.equals(F6) || baseEnemyType.equals(F7)) {
            Enemy boss;
            GameConfig gameConfig = playGameState.getRoom().getGame().getConfig(seat);
            MinMaxParams minMaxParams = gameConfig.getPercentPayOnBoss().get(baseEnemy.getEnemyType());
            Long anyBossId = playGameState.getMap().getAnyBossId();

            if (playGameState.isBossRound() && minMaxParams != null && minMaxParams.getMax() > 0 && anyBossId != 1) {
                boss = playGameState.getMap().getItemById(anyBossId);
                if (boss != null && playGameState.isEnemyAwayFromBorder(boss, baseEnemyType)) {
                    double percentRangeMode = 10;
                    double rageThreshold = (boss.getFullEnergy() * percentRangeMode) / 100;
                    boolean regularBossMode = boss.getEnergy() > rageThreshold;
                    if (regularBossMode) {
                        int randomPay = RNG.nextInt((int) (minMaxParams.getMin() * 100), (int) (minMaxParams.getMax() * 100));
                        double averagePercent = ((double) randomPay) / 100;

                        EnemySpecialItem enemySpecialItem = (EnemySpecialItem) baseEnemy;
                        int totalPayout = enemySpecialItem.getTotalPayout();
                        int randomBossPay = (int) (averagePercent * totalPayout);

                        if (randomBossPay > boss.getEnergy()) {
                            randomBossPay = (int) boss.getEnergy();
                        }

                        getLog().debug("Calculated RandomBoss Payout: {}, averagePercent: {}, totalPayout: {}, bossEnergy: {}",
                                randomBossPay, averagePercent, totalPayout, boss.getEnergy());
                        return new Result(randomBossPay);
                    }
                }
            }
        }

        return new Result();
    }

    public Result calculateMissPay() {
        EnemySpecialItem enemySpecialItem = (EnemySpecialItem) baseEnemy;
        int additionalKillAwardPayout = (int) enemySpecialItem.getAdditionalKillAwardPayout();

        // If boss pay is greater than additional kill award payout, then we need to calculate the miss payout
        // otherwise we can just return 0
        if (bossRandomPayResult.getPay() > 0 && additionalKillAwardPayout <= bossRandomPayResult.getPay()) {
            int missPay = bossRandomPayResult.getPay() - additionalKillAwardPayout;

            getLog().debug("Calculated Miss Payout: {}", missPay);
            return new Result(missPay);
        }

        return new Result(0);
    }

    public Result calculateNewAdditionalKillAwardPay() {

        EnemySpecialItem enemySpecialItem = (EnemySpecialItem) baseEnemy;
        long additionalKillAwardPayout = enemySpecialItem.getAdditionalKillAwardPayout();

        // If boss pay is greater than additional kill award payout, then we need to calculate the new additional kill award payout
        // else we just return the current additional kill award payout
        if (bossRandomPayResult.getPay() > 0 && additionalKillAwardPayout > bossRandomPayResult.getPay()) {
            int newAdditionalKillAwardPayout = (int) additionalKillAwardPayout - bossRandomPayResult.getPay();

            getLog().debug("Calculated NewAdditionalKillAward Payout: {}", newAdditionalKillAwardPayout);
            return new Result(newAdditionalKillAwardPayout);
        }

        return new Result((int) additionalKillAwardPayout);
    }

    public Result calculateEnemyPay() {
        if (EnemyRange.SPECIAL_ITEMS.contains(baseEnemy.getEnemyType()) && !baseEnemy.getEnemyType().equals(F1)) {
            EnemySpecialItem enemySpecialItem = (EnemySpecialItem) baseEnemy;
            int chMult = enemySpecialItem.getCurrentMultiplier();
            if (chMult == 0) {
                chMult = 1;
            }

            int killAwardFromEnemies = calculateNewAdditionalKillAwardPay().getPay() & chMult;
            int missPay = calculateMissPay().getPay();

            List<EnemyType> enemiesForKilling = enemySpecialItem.getEnemiesForKilling();
            for (EnemyType killEnemyType : enemiesForKilling) {
                GameConfig gameConfig = playGameState.getRoom().getGame().getConfig(seat);
                int enemyPayout = MathData.getEnemyPayout(gameConfig, killEnemyType, 0);

                if (missPay > 0) {
                    missPay -= enemyPayout;
                    if (missPay < 0) {
                        int additionalMissPayout = Math.abs(missPay);
                        killAwardFromEnemies += additionalMissPayout * chMult;
                        missPay = 0;
                    }
                } else {

                    killAwardFromEnemies += chMult * enemyPayout;
                }

                getLog().debug("Calculated Enemy Payout: {}, enemy: {}", enemyPayout, killEnemyType);
            }

            getLog().debug("Calculated Total Enemy Payout: {}", killAwardFromEnemies);
            return new Result(killAwardFromEnemies);
        }

        return new Result();
    }

    public Result getFinalResult() {
        int totalPay = 0;

        totalPay += freezeResult.getPay();
        totalPay += bossRandomPayResult.getPay();
        totalPay += enemyPay.getPay();

        getLog().debug("Calculated Total Payout: {}", totalPay);

        return new Result(totalPay);
    }
}
