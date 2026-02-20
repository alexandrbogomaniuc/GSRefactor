package com.betsoft.casino.mp.missionamazon.model.math;

import com.betsoft.casino.mp.common.math.EnemyPays;
import com.betsoft.casino.mp.common.math.Paytable;
import com.betsoft.casino.mp.common.math.Prize;
import com.betsoft.casino.mp.common.math.SWPaidCosts;
import com.betsoft.casino.mp.missionamazon.model.math.config.GameConfig;
import com.betsoft.casino.mp.missionamazon.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.Gem;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

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
            if (specialWeaponType.getAvailableGameIds().contains((int) GameType.MISSION_AMAZON.getGameId())) {
                weaponPaidMultiplier.add(
                        new SWPaidCosts(specialWeaponType.getId(), MathData.getPaidWeaponCost(config, specialWeaponType.getId())));
            }
        });
        Paytable paytable = new Paytable(enemyPayouts, 0, null, weaponPaidMultiplier,
                null, null, null, MathData.getPossibleBetLevels());
        paytable.setGemPayouts(getGemsPayout(config));
        LOG.debug("createPayTable: new paytable={}", paytable);
        return paytable;
    }

    public static Paytable getTable() {
        return TABLE;
    }

    private static Map<Integer, List<Integer>> getGemsPayout(GameConfig config) {
        return Arrays.stream(Gem.values()).
                collect(Collectors.toMap(Gem::getId, e -> Arrays.asList(config.getQuestParams().getMinPrize(), config.getQuestParams().getMaxPrize())));
    }

    private static void fillUpEnemiesPayouts(List<EnemyPays> enemyPayouts, GameConfig config) {
        config.getEnemies().forEach((enemyType, enemyData) -> {
            int payout = enemyData.get(0).getPayout();
            EnemyPays enemyPays = new EnemyPays(enemyType.getId(), enemyType.getName(), new Prize(payout, payout));
            enemyPayouts.add(enemyPays);
        });
    }

    private static void fillUpBossPayouts(List<EnemyPays> enemyPayouts, GameConfig config) {
        Collection<Map<Integer, Double>> values = config.getBossParams().getBossPays().getPartialPaysWeights().values();
        List<Integer> pays = new ArrayList<>();
        for (Map<Integer, Double> value : values) {
            pays.addAll(value.keySet());
        }
        for (BossType bossType : BossType.values()) {
            EnemyPays enemyPay = new EnemyPays(EnemyType.BOSS.getId(), bossType.name(),
                    new Prize(Collections.min(pays), Collections.max(pays)));
            enemyPayouts.add(enemyPay);
        }
    }
}



