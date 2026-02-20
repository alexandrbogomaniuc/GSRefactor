package com.betsoft.casino.mp.sectorx.model.math;

import com.betsoft.casino.mp.common.math.EnemyPays;
import com.betsoft.casino.mp.common.math.Paytable;
import com.betsoft.casino.mp.common.math.Prize;
import com.betsoft.casino.mp.common.math.SWPaidCosts;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.sectorx.model.math.config.GameConfig;
import com.betsoft.casino.mp.sectorx.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.sectorx.model.math.config.SpecialItem;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;

import static com.betsoft.casino.mp.model.SpecialWeaponType.values;

public class PayTableInst {
    private static final Logger LOG = LogManager.getLogger(PayTableInst.class);
    private static final Paytable TABLE = createPayTable();

    private PayTableInst() {
    }

    private static Paytable createPayTable() {
        LOG.debug("createPayTable start");
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        List<EnemyPays> enemyPayouts = Lists.newArrayListWithCapacity(EnemyType.values().length + BossType.values().length);
        fillUpEnemiesPayouts(enemyPayouts, config);
        fillUpBossPayouts(enemyPayouts, config);

        List<SWPaidCosts> weaponPaidMultiplier = new ArrayList<>();
        Arrays.stream(values()).forEach(specialWeaponType -> {
            if (specialWeaponType.getAvailableGameIds().contains((int) GameType.SECTOR_X.getGameId())) {
                weaponPaidMultiplier.add(
                        new SWPaidCosts(specialWeaponType.getId(), MathData.getPaidWeaponCost(config, specialWeaponType.getId())));
            }
        });
        Paytable paytable = new Paytable(enemyPayouts, 0, null, weaponPaidMultiplier,
                null, null, null, MathData.getPossibleBetLevels());
        LOG.debug("createPayTable: new paytable={}", paytable);
        return paytable;
    }

    public static Paytable getTable() {
        return TABLE;
    }

    private static void fillUpEnemiesPayouts(List<EnemyPays> enemyPayouts, GameConfig config) {
        config.getEnemies().forEach((enemyType, enemyData) -> {
            boolean isHugeEnemy = EnemyRange.HUGE_PAY_ENEMIES.contains(enemyType);
            if (isHugeEnemy) {
                int minPayout = enemyData.get(0).getMinPay();
                int maxPayout = enemyData.get(0).getMaxPay();
                EnemyPays enemyPays = new EnemyPays(enemyType.getId(), enemyType.getName(), new Prize(minPayout, maxPayout));
                enemyPayouts.add(enemyPays);
            } else {
                int payout = enemyData.get(0).getPay();
                EnemyPays enemyPays = new EnemyPays(enemyType.getId(), enemyType.getName(), new Prize(payout, payout));
                enemyPayouts.add(enemyPays);
            }
        });

        config.getItems().forEach((enemyType, enemyData) -> {
            SpecialItem specialItem = enemyData.get(0);

            if (Arrays.asList(EnemyType.F1, EnemyType.F7).contains(enemyType)) {
                for (com.betsoft.casino.mp.sectorx.model.math.config.Prize prize : specialItem.getPrizes()) {
                    EnemyPays enemyPays = new EnemyPays(enemyType.getId(), enemyType.getName(), new Prize(prize.getPay(), prize.getPay()));
                    enemyPayouts.add(enemyPays);
                }
            } else if (EnemyType.F4.equals(enemyType)) {
                for (com.betsoft.casino.mp.sectorx.model.math.config.Prize prize : specialItem.getPrizes()) {
                    EnemyPays enemyPays = new EnemyPays(enemyType.getId(), enemyType.getName(), new Prize(prize.getMinPay(), prize.getMaxPay()));
                    enemyPayouts.add(enemyPays);
                }
            } else {
                Function<com.betsoft.casino.mp.sectorx.model.math.config.Prize, Integer> getPay = com.betsoft.casino.mp.sectorx.model.math.config.Prize::getPay;

                int minPayout = specialItem.getPrizes().stream()
                        .min(Comparator.comparing(getPay)).get().getPay();

                int maxPayout = specialItem.getPrizes().stream()
                        .max(Comparator.comparing(getPay)).get().getPay();

                EnemyPays enemyPays = new EnemyPays(enemyType.getId(), enemyType.getName(), new Prize(minPayout, maxPayout));
                enemyPayouts.add(enemyPays);
            }
        });
    }

    private static void fillUpBossPayouts(List<EnemyPays> enemyPayouts, GameConfig config) {
        config.getBosses().forEach((skinId, boss) -> {
            EnemyPays bossPays = new EnemyPays(EnemyType.BOSS.getId(), BossType.getBySkinId(skinId).name(),
                    new Prize(boss.getMinPay(), boss.getMaxPay()));
            enemyPayouts.add(bossPays);
        });
    }
}



