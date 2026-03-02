package com.betsoft.casino.mp.pirates.model.math;

import com.betsoft.casino.mp.common.math.*;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.model.SpecialWeaponType.values;

public class PayTableInst {
    private static final Logger LOG = LogManager.getLogger(PayTableInst.class);

    private static Paytable TABLE = createPayTable();


    private static Paytable createPayTable() {
        List<EnemyPays> enemyPayouts = new ArrayList<>(EnemyType.values().length);
        List<LootboxPrizes> lootboxPrizes = new ArrayList<>();
        int energyBoss = fillUpEnemyPayoutsAndGetBossEnergy(enemyPayouts);
        fillUpBossPayouts(enemyPayouts);
        fillUpLootboxPrizes(lootboxPrizes);
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

        Paytable paytable = new Paytable(enemyPayouts, energyBoss, lootboxPrizes, new ArrayList<>(),
                new HashMap<>(), healthByLevels, getEnemyPayoutsByWeapons());
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

    private static void fillUpLootboxPrizes(List<LootboxPrizes> lootboxPrizes) {
        for (int i = 0; i < 3; i++) {
            Map<Integer, Pair<Integer, Integer>> weapons = new HashMap<>();
            List<WeaponLootBoxProb.WeaponEntry> table = WeaponLootBoxProb.getTables200().get(0);
            for (WeaponLootBoxProb.WeaponEntry entry : table) {
                if (!weapons.containsKey(entry.getType())) {
                    weapons.put(entry.getType(), new Pair<>(10000, -1));
                }
                Pair<Integer, Integer> minMax = weapons.get(entry.getType());
                int shots = entry.getShots();

                if (i == 1) shots = 2* shots;
                if (i == 2) shots = 3* shots;

                if (shots < minMax.getKey()) {
                    minMax.setKey(shots);
                }
                if (shots > minMax.getValue()) {
                    minMax.setValue(shots);
                }
            }

            SpecialWeaponType[] weaponTypes = values();
            List<LootboxPrize> prizeList = new ArrayList<>(weaponTypes.length);
            for (SpecialWeaponType weaponType : weaponTypes) {
                Pair<Integer, Integer> pair = weapons.get(weaponType.ordinal());
                if(pair!=null) {
                    prizeList.add(new LootboxPrize(weaponType.ordinal(), pair.getKey(), pair.getValue()));
                }
            }
            lootboxPrizes.add(new LootboxPrizes(i, prizeList));
        }
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
                if (weaponType.getAvailableGameIds().contains(808)) weapons.add(weaponType.getId());
            }
            for (Integer weapon : weapons) {
                HashSet<Long> possiblePayouts = new HashSet<>();
                if (levels == null) levels = new int[]{0};
                for (int i = 0; i < levels.length; i++) {
                    WeaponData weaponData = enemyData.getWeaponDataMap(weapon, i);
                    if (weaponData.getEnemyLowHitPointMap() != null)
                        possiblePayouts.addAll(weaponData.getEnemyLowHitPointMap().keySet());
                    if (weaponData.getEnemyMiddleHitPointMap() != null)
                        possiblePayouts.addAll(weaponData.getEnemyMiddleHitPointMap().keySet());
                    if (weaponData.getEnemyHiHitPointMap() != null)
                        possiblePayouts.addAll(weaponData.getEnemyHiHitPointMap().keySet());
                }
                Map<Integer, Prize> weaponsOrDefault = enemyPayoutsByWeapons.getOrDefault(enemyId, new HashMap<>());
                possiblePayouts.remove(0L);
                Set<Double> updated = possiblePayouts.stream().map(v -> v * 0.1).collect(Collectors.toSet());
                Double min = Collections.min(updated);
                Double max = Collections.max(updated);
                weaponsOrDefault.put(weapon, new Prize(min, max));
                enemyPayoutsByWeapons.put(enemyId, weaponsOrDefault);
            }
        }
        return enemyPayoutsByWeapons;
    }


}



