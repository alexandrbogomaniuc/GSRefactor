package com.betsoft.casino.mp.piratescommon.model.math;

import com.betsoft.casino.mp.common.math.*;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.piratescommon.model.math.data.IEnemyData;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.model.SpecialWeaponType.values;

public class PayTableInst {
    private static final Logger LOG = LogManager.getLogger(PayTableInst.class);

    private static Paytable TABLE = createPayTable();


    private static Paytable createPayTable() {
        LOG.debug("createPayTable start");
        List<EnemyPays> enemyPayouts = new ArrayList<>(EnemyType.values().length);
        List<LootboxPrizes> lootboxPrizes = new ArrayList<>();
        int energyBoss = fillUpEnemyPayoutsAndGetBossEnergy(enemyPayouts);
        fillUpBossPayouts(enemyPayouts);
        Map<Integer, List<Integer>> healthByLevels = new HashMap<>();
        for (EnemyType enemyType : EnemyType.values()) {
            if(enemyType.isBoss()){
                List<Triple<Integer, Integer, Double>> bossParams = MathData.getBossParams();
                Integer[] params = new Integer[bossParams.size()];
                bossParams.forEach(triple -> params[triple.first()] = triple.second());
                healthByLevels.put(enemyType.getId(), Arrays.asList(params));
            }else {
                EnemyData enemyData = MathData.getEnemyData(enemyType.getId());
                healthByLevels.put(enemyType.getId(), Arrays.asList(Arrays.stream(enemyData.getLevels()).boxed()
                        .toArray( Integer[]::new)));
            }
        }

        List<SWPaidCosts> weaponPaidMultiplier = new ArrayList<>();
        Arrays.stream(values()).forEach(specialWeaponType -> {
            if (specialWeaponType.getAvailableGameIds().contains((int) GameType.PIRATES_POV.getGameId())) {
                weaponPaidMultiplier.add(
                        new SWPaidCosts(specialWeaponType.getId(), MathData.getPaidWeaponCost(specialWeaponType.getId())));
            }
        });
        Paytable paytable = new Paytable(enemyPayouts, energyBoss, lootboxPrizes, weaponPaidMultiplier,
                MathQuestData.questsCacheWins.values().stream()
                        .map(Double::intValue)
                        .collect(Collectors.toList()),
                healthByLevels, getEnemyPayoutsByWeapons(), MathData.getPossibleBetLevels());

        LOG.debug("createPayTable: new paytable={}", paytable);
        return paytable;
    }

    private static int fillUpEnemyPayoutsAndGetBossEnergy(List<EnemyPays> enemyPayouts) {
        for (EnemyType enemyType : EnemyType.values()) {
            if (!enemyType.isBoss())
                enemyPayouts.add(new EnemyPays(enemyType.getId(), enemyType.getName(), new Prize(enemyType.getReward(),
                        enemyType.getReward())));
        }
        return 100;
    }

    private static void fillUpBossPayouts(List<EnemyPays> enemyPayouts) {
        enemyPayouts.add(new EnemyPays(EnemyType.Boss.getId(), "1", new Prize(0, 2500)));
        enemyPayouts.add(new EnemyPays(EnemyType.Boss.getId(), "2", new Prize(0, 5000)));
        enemyPayouts.add(new EnemyPays(EnemyType.Boss.getId(), "3", new Prize(0, 7500)));
    }

    public static Paytable getTable() {
        return TABLE;
    }


    public static Map<Integer, Map<Integer, Prize>> getEnemyPayoutsByWeapons() {
        Map<Integer, Map<Integer, Prize>> enemyPayoutsByWeapons = new HashMap<>();
        for (EnemyType enemyType : EnemyType.values()) {
            int enemyId = enemyType.getId();
            EnemyData enemyData = MathData.getEnemyData(enemyId);
            int[] levels = enemyData.getLevels();

            HashSet<Integer> weapons = new HashSet<>();
            weapons.add(-1);
            for (SpecialWeaponType weaponType : SpecialWeaponType.values()) {
                if (weaponType.getAvailableGameIds().contains((int) GameType.PIRATES_POV.getGameId())) weapons.add(weaponType.getId());
            }
            for (Integer weapon : weapons) {
                Map<Integer, HashSet<Long>> possiblePayoutsMap = new HashMap<>();
                if (levels == null) levels = new int[]{0};
                if (enemyType.isBoss()) levels = new int[]{0, 1, 2};
                for (int i = 0; i < levels.length; i++) {
                    HashSet<Long> possiblePayouts = new HashSet<>();
                    WeaponData weaponData = enemyData.getWeaponDataMap(weapon, i);
                    if (weaponData.getEnemyLowHitPointMap() != null)
                        possiblePayouts.addAll(weaponData.getEnemyLowHitPointMap().keySet());
                    if (weaponData.getEnemyMiddleHitPointMap() != null)
                        possiblePayouts.addAll(weaponData.getEnemyMiddleHitPointMap().keySet());
                    if (weaponData.getEnemyHiHitPointMap() != null)
                        possiblePayouts.addAll(weaponData.getEnemyHiHitPointMap().keySet());
                    possiblePayoutsMap.put(i, possiblePayouts);
                }

                Map<Integer, Prize> weaponsOrDefault = enemyPayoutsByWeapons.getOrDefault(enemyId, new HashMap<>());
                if (enemyType.isBoss()) {
                    int idx = 0;
                    for (Map.Entry<Integer, HashSet<Long>> integerHashSetEntry : possiblePayoutsMap.entrySet()) {
                        HashSet<Long> possiblePayouts = integerHashSetEntry.getValue();
                        possiblePayouts.remove(0L);
                        Set<Double> updated = possiblePayouts.stream().map(v -> v * 0.1).collect(Collectors.toSet());
                        Double min = Collections.min(updated);
                        Double max = Collections.max(updated);
                        weaponsOrDefault.put(weapon + 100 * idx, new Prize(min, max));
                        enemyPayoutsByWeapons.put(enemyId, weaponsOrDefault);
                        idx++;
                    }
                } else {
                    HashSet<Long> possiblePayouts = new HashSet<>();
                    for (Map.Entry<Integer, HashSet<Long>> integerHashSetEntry : possiblePayoutsMap.entrySet()) {
                        possiblePayouts.addAll(integerHashSetEntry.getValue());
                    }
                    possiblePayouts.remove(0L);
                    Set<Double> updated = possiblePayouts.stream().map(v -> v * 0.1).collect(Collectors.toSet());
                    Double min = Collections.min(updated);
                    Double max = Collections.max(updated);
                    weaponsOrDefault.put(weapon, new Prize(min, max));
                    enemyPayoutsByWeapons.put(enemyId, weaponsOrDefault);
                }
            }
        }
        return enemyPayoutsByWeapons;
    }

}



