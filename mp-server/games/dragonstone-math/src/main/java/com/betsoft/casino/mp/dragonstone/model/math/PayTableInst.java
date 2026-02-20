package com.betsoft.casino.mp.dragonstone.model.math;

import com.betsoft.casino.mp.common.math.*;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.dragonstone.model.math.slot.MiniSlot;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.dragonstone.model.math.EnemyRange.SPECIAL_ENEMIES;
import static com.betsoft.casino.mp.dragonstone.model.math.EnemyRange.SPECTERS;
import static com.betsoft.casino.mp.model.SpecialWeaponType.values;

public class PayTableInst {
    private static final Logger LOG = LogManager.getLogger(PayTableInst.class);

    private static Paytable TABLE = createPayTable();

    private static Paytable createPayTable() {
        LOG.debug("createPayTable start");
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        List<EnemyPays> enemyPayouts = new ArrayList<>(EnemyType.values().length);
        List<LootboxPrizes> lootBoxPrizes = new ArrayList<>();
        config.getEnemies().forEach((enemyType, enemyData) -> {
            EnemyPays enemyPays;

            if (SPECTERS.contains(enemyType) || SPECIAL_ENEMIES.contains(enemyType)) {
                enemyPays = new EnemyPays(enemyType.getId(), enemyType.getName(), new Prize(getMinPay(enemyData), getMaxPay(enemyData)));
            } else if (enemyType.equals(EnemyType.OGRE)) {
                enemyPays = new EnemyPays(enemyType.getId(), enemyType.getName(),
                        new Prize(Math.min(config.getRageMin(), getMinPay(enemyData)), Math.max(config.getRageMax(), getMaxPay(enemyData))));
            } else {
                enemyPays = new EnemyPays(enemyType.getId(), enemyType.getName(), new Prize(getPay(enemyData, EnemyData::getPayout, Collections::max),
                        getPay(enemyData, EnemyData::getPayout, Collections::max)));

            }
            enemyPayouts.add(enemyPays);
        });

        List<Integer> bossPayouts = new ArrayList<>(config.getBoss().getSmallPays().keySet());
        bossPayouts.add(config.getBoss().getKilledPay());
        EnemyPays bossPays = new EnemyPays(EnemyType.DRAGON.getId(), EnemyType.DRAGON.getName(),
                new Prize(Collections.min(bossPayouts), Collections.max(bossPayouts)));
        enemyPayouts.add(bossPays);

        List<SWPaidCosts> weaponPaidMultiplier = new ArrayList<>();
        Arrays.stream(values()).forEach(specialWeaponType -> {
            if (specialWeaponType.getAvailableGameIds().contains((int) GameType.DRAGONSTONE.getGameId())) {
                weaponPaidMultiplier.add(
                        new SWPaidCosts(specialWeaponType.getId(), MathData.getPaidWeaponCost(config, specialWeaponType.getId())));
            }
        });
        Paytable paytable = new Paytable(enemyPayouts, 0, lootBoxPrizes, weaponPaidMultiplier,
                null, null, getEnemyPayoutsByWeapons(), MathData.getPossibleBetLevels());
        paytable.setReels(createReels());

        LOG.debug("createPayTable: new paytable={}", paytable);
        return paytable;
    }

    private static Integer getMinPay(List<EnemyData> enemyData) {
        return getPay(enemyData, EnemyData::getMinPay, Collections::min);
    }

    private static Integer getMaxPay(List<EnemyData> enemyData) {
        return getPay(enemyData, EnemyData::getMaxPay, Collections::max);
    }

    private static Integer getPay(List<EnemyData> enemyData, ToIntFunction<EnemyData> function, Function<List<Integer>, Integer> operation) {
        List<Integer> pays = enemyData.stream().mapToInt(function).boxed().collect(Collectors.toList());
        return operation.apply(pays);
    }

    public static Paytable getTable() {
        return TABLE;
    }

    public static Map<Integer, Map<Integer, Prize>> getEnemyPayoutsByWeapons() {
        Map<Integer, Map<Integer, Prize>> enemyPayoutsByWeapons = new HashMap<>();
        for (EnemyType enemyType : EnemyType.values()) {
            if (!SPECTERS.contains(enemyType)) {
                int enemyId = enemyType.getId();
                HashSet<Integer> weapons = new HashSet<>();
                weapons.add(-1);
                for (SpecialWeaponType weaponType : SpecialWeaponType.values()) {
                    if (weaponType.getAvailableGameIds().contains(838)) {
                        weapons.add(weaponType.getId());
                    }
                }
            }
        }
        return enemyPayoutsByWeapons;
    }

    private static Map<Integer, List<Integer>> createReels() {
        Map<Integer, List<Integer>> reels = new HashMap<>();
        int i = 1;
        for (int[] reel : MiniSlot.getReels()) {
            reels.put(i++, Arrays.stream(reel).boxed().collect(Collectors.toList()));
        }
        return reels;
    }
}



