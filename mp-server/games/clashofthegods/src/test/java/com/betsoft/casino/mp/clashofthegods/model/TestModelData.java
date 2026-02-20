package com.betsoft.casino.mp.clashofthegods.model;

import com.betsoft.casino.mp.clashofthegods.model.math.EnemyType;
import com.betsoft.casino.mp.clashofthegods.model.math.MathData;
import com.betsoft.casino.mp.clashofthegods.model.math.WeaponData;
import com.betsoft.casino.mp.clashofthegods.model.math.enemies.IEnemyData;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.gameconfig.GameTools;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestModelData {
    public static void main(String[] args) {
        boolean paidSpecialShot = false;

        Set<Integer> weapons = new HashSet<>();
        weapons.add(-1);
        for (SpecialWeaponType value : SpecialWeaponType.values()) {
            if(value.getAvailableGameIds().contains((int)GameType.CLASH_OF_THE_GODS.getGameId()))
                weapons.add(value.getId());
        }

        for (EnemyType value : EnemyType.values()) {
            if(!value.isBoss()) {
                IEnemyData enemyData = MathData.getEnemyData(value.getId());
                for (Integer weapon : weapons) {
                    if(value.equals(EnemyType.Golden_Dragon)){
                        int ii =0;
                    }
                    int[] levels = enemyData.getLevels();
                    for (int i = 0; i < levels.length; i++) {
                        WeaponData weaponDataMap = enemyData.getWeaponDataMap(weapon, i, false);
                        Map<Long, Double> enemyHiHitPointMap = weaponDataMap.getEnemyHiHitPointMap();
                        if(enemyHiHitPointMap!=null && !enemyHiHitPointMap.isEmpty()){
                            double sum = enemyHiHitPointMap.values().stream().mapToDouble(Double::doubleValue).sum();
                            if(sum - 1.0 >= 0.00001){
                                System.out.println("hiMap, weapon: " + weapon +" enemy: " + value.name() + ", summ: " + sum );
                            }
                        }
                        Map<Long, Double> enemyLowHitPointMap = weaponDataMap.getEnemyLowHitPointMap();
                        if(enemyLowHitPointMap!=null && !enemyLowHitPointMap.isEmpty()){
                            double sum = enemyLowHitPointMap.values().stream().mapToDouble(Double::doubleValue).sum();
                            if((sum - 1.0) >= 0.00001){
                                System.out.println("lowMap, weapon: " + weapon +" enemy: " + value.name() + ", summ: " + sum );
                            }
                        }
                    }
                }
            }
        }

//        for (SpecialWeaponType value : SpecialWeaponType.values()) {
//            if(value.getAvailableGameIds().contains((int)GameType.CLASH_OF_THE_GODS.getGameId())){
//                System.out.println(value.getTitle() + "  RTP usual:" + MathData.getRtpForWeapon(value.getId())
//                        + " full RTP:" + MathData.getFullRtpForWeapon(value.getId()) ) ;
//
//            }
//        }


//        for (EnemyType enemyType : EnemyType.values()) {
//
//
//            int wpId = -1;
//            IEnemyData enemyData = MathData.getEnemyData(enemyType.getId());
//
//            if(enemyData.getLevels() == null) {
//                System.out.println(enemyType);
//                continue;
//            }
//
//            if(enemyData.getLevels() != null) {
//                for (int i = 0; i < enemyData.getLevels().length; i++) {
//                    int level = i;
//
//                    WeaponData weaponData = enemyData.getWeaponDataMap(wpId, level, paidSpecialShot);
//                    Map<Long, Double> enemyLowHitPointMap = weaponData.getEnemyLowHitPointMap();
//                    Map<Long, Double> enemyHiHitPointMap = weaponData.getEnemyHiHitPointMap();
//                    double avgPayoutHiMap = (enemyData.getSwAvgPayouts(wpId, level, MathData.PAY_HIT_PERCENT, paidSpecialShot, enemyHiHitPointMap));
//                    double avgPayoutLowMap = 0;
//                    boolean thereAreTwoMaps = enemyLowHitPointMap != null && !enemyLowHitPointMap.isEmpty();
//                    if (thereAreTwoMaps) {
//                        avgPayoutLowMap = enemyData.getSwAvgPayouts(wpId, level, MathData.PAY_HIT_PERCENT, paidSpecialShot, enemyLowHitPointMap);
//                    }
//
//                    System.out.println(enemyType.name() + " level: " + level + " weapon: Turret"
//                            + "  avgPayoutHiMap: " + avgPayoutHiMap
//                    );
//
////                if (thereAreTwoMaps && (avgPayoutHiMap != avgPayoutLowMap)) {
////                }
//                }
//            }else{
//
//
//            }
//
//        }
//
//        for (EnemyType enemyType : EnemyType.values()) {
//            for (SpecialWeaponType value : SpecialWeaponType.values()) {
//                if (value.getAvailableGameIds().contains((int) GameType.CLASH_OF_THE_GODS.getGameId())) {
//                    int wpId = value.getId();
//                    int levelId = 1;
//                    IEnemyData enemyData = MathData.getEnemyData(enemyType.getId());
//                    ;
//                    WeaponData weaponData = enemyData.getWeaponDataMap(wpId, 1, paidSpecialShot);
//                    double rtpForWeapon = MathData.getRtpForWeapon(wpId);
//                    Map<Long, Double> enemyLowHitPointMap = weaponData.getEnemyLowHitPointMap();
//                    Map<Long, Double> enemyHiHitPointMap = weaponData.getEnemyHiHitPointMap();
//                    int avgPayoutHiMap = (enemyData.getSwAvgPayouts(wpId, levelId, MathData.PAY_HIT_PERCENT, paidSpecialShot, enemyHiHitPointMap)).intValue();
//                    int avgPayoutLowMap = 0;
//                    boolean thereAreTwoMaps = enemyLowHitPointMap != null && !enemyLowHitPointMap.isEmpty();
//                    if (thereAreTwoMaps) {
//                        avgPayoutLowMap = enemyData.getSwAvgPayouts(wpId, levelId, MathData.PAY_HIT_PERCENT, paidSpecialShot, enemyLowHitPointMap).intValue();
//                    }
//
//                    if (thereAreTwoMaps && (avgPayoutHiMap != avgPayoutLowMap)) {
//                        System.out.println(enemyType.name() + " weapon: " + value.getTitle()
//                                + "  avgPayoutHiMap: " + avgPayoutHiMap + " avgPayoutLowMap: " + avgPayoutLowMap);
//                    }
//                    //System.out.println("rtpForWeapon: " + rtpForWeapon);
//
//
//                }
//            }
//        }

//        for (SpecialWeaponType value : SpecialWeaponType.values()) {
//            if(value.getAvailableGameIds().contains(GameType.CLASH_OF_THE_GODS.getGameId())){
//                int wpId = value.getId();
//                int levelId = 1;
//                boolean paidSpecialShot = true;
//                IEnemyData enemyData = MathData.getEnemyData(EnemyType.Lantern.getId());;
//                WeaponData weaponData = enemyData.getWeaponDataMap(wpId, 0, paidSpecialShot);
//                double rtpForWeapon = MathData.getRtpForWeapon(wpId);
//                Double avgPayout = enemyData.getSwAvgPayouts(wpId, levelId, MathData.PAY_HIT_PERCENT, paidSpecialShot);
//
//                System.out.println("weaponData: " + weaponData);
//                System.out.println("rtpForWeapon: " + rtpForWeapon);
//                System.out.println("avgPayout: " + avgPayout);
//
//            }
//        }
//
//        int wpId = SpecialWeaponType.Ricochet.getId();
//        int levelId = 1;
//        boolean paidSpecialShot = true;
//        IEnemyData enemyData = MathData.getEnemyData(EnemyType.Lantern.getId());;
//        WeaponData weaponData = enemyData.getWeaponDataMap(wpId, 0, paidSpecialShot);
//        double rtpForWeapon = MathData.getRtpForWeapon(wpId);
//        Double avgPayout = enemyData.getSwAvgPayouts(wpId, levelId, MathData.PAY_HIT_PERCENT, paidSpecialShot);
//
//        System.out.println("weaponData: " + weaponData);
//        System.out.println("rtpForWeapon: " + rtpForWeapon);
//        System.out.println("avgPayout: " + avgPayout);
//        int cnt = 100;
//        List<Triple<Integer, Integer, Double>> probOfDropSpecialWeaponsWeaponCarrier
//                = MathData.getProbOfDropSpecialWeaponsWeaponCarrier();
//
//        Map<Pair<Integer, Integer>, Integer> coasts = new HashMap<>();
//        coasts.put(new Pair<>(SpecialWeaponType.Bomb.getId(), 12), 0);
//        coasts.put(new Pair<>(SpecialWeaponType.Bomb.getId(), 15), 0);
//        coasts.put(new Pair<>(SpecialWeaponType.Bomb.getId(), 18), 0);
//
//        coasts.put(new Pair<>(SpecialWeaponType.Landmines.getId(), 12), 0);
//        coasts.put(new Pair<>(SpecialWeaponType.Landmines.getId(), 15), 0);
//        coasts.put(new Pair<>(SpecialWeaponType.Landmines.getId(), 18), 0);
//
//        coasts.put(new Pair<>(SpecialWeaponType.Cryogun.getId(), 5), 0);
//        coasts.put(new Pair<>(SpecialWeaponType.Cryogun.getId(), 6), 0);
//        coasts.put(new Pair<>(SpecialWeaponType.Cryogun.getId(), 7), 0);
//
//        coasts.put(new Pair<>(SpecialWeaponType.Flamethrower.getId(), 7), 0);
//        coasts.put(new Pair<>(SpecialWeaponType.Flamethrower.getId(), 8), 0);
//        coasts.put(new Pair<>(SpecialWeaponType.Flamethrower.getId(), 10), 0);
//
//        coasts.put(new Pair<>(SpecialWeaponType.Ricochet.getId(), 15), 0);
//        coasts.put(new Pair<>(SpecialWeaponType.Ricochet.getId(), 18), 0);
//        coasts.put(new Pair<>(SpecialWeaponType.Ricochet.getId(), 20), 0);
//
//        coasts.put(new Pair<>(SpecialWeaponType.ArtilleryStrike.getId(), 5), 0);
//        coasts.put(new Pair<>(SpecialWeaponType.ArtilleryStrike.getId(), 6), 0);
//        coasts.put(new Pair<>(SpecialWeaponType.ArtilleryStrike.getId(), 7), 0);
//
//
//        for (int i = 0; i < cnt; i++) {
//            Pair<Integer, Integer> randomPair = GameTools.getRandomPair(probOfDropSpecialWeaponsWeaponCarrier);
//            Integer integer = coasts.get(randomPair);
//            coasts.put(randomPair, integer + 1);
//        }
//
//        int sum = 0;
//        for (Map.Entry<Pair<Integer, Integer>, Integer> pairIntegerEntry : coasts.entrySet()) {
//            Pair<Integer, Integer> key = pairIntegerEntry.getKey();
//            String title = SpecialWeaponType.values()[key.getKey()].getTitle();
//            System.out.println(title + "  " +  key.getValue() + "    " + pairIntegerEntry.getValue()
//                    + "  " + pairIntegerEntry.getValue() / (double)cnt);
//
//            sum += pairIntegerEntry.getValue();
//        }
//
//        System.out.println("sum: " + sum);


//        for (int i = -1; i < SpecialWeaponType.values().length-1; i++) {
//            Pair<Double, Double> probabilitiesForBossByWeapon = MathData.getProbabilitiesForBossByWeapon(i);
//            System.out.println("i: " + i + "    " + probabilitiesForBossByWeapon);
//        }


//        int keyId = 0;
//        Map<Integer, Integer> realDataForKey = MathData.getProbOfDropSpecialWeaponsForKeys().entrySet()
//                .stream().collect(Collectors.toMap(Map.Entry::getKey,
//                e -> keyId == 0 ? e.getValue().first() : (keyId == 1 ? e.getValue().second() : e.getValue().third())));
//
//        System.out.println(realDataForKey);
//        double sum = realDataForKey.values().stream().mapToInt(Integer::intValue).sum();
//
//        Map<Integer, Double> normRealData =
//                realDataForKey.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() / sum));
//
//        System.out.println(normRealData);
//
//        double sum2 = normRealData.values().stream().mapToDouble(Double::doubleValue).sum();
//
//        System.out.println("sum: " + sum + " sum2: " + sum2);


//        for (int i = 0; i < 10000; i++) {
//            Pair<Integer, Integer> weaponForKey = MathData.getWeaponForKey(RNG.nextInt(2) + 1);
//            System.out.println(weaponForKey);
//        }
    }
}
