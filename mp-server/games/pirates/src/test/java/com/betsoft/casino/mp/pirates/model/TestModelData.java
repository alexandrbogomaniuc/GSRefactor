package com.betsoft.casino.mp.pirates.model;

import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.pirates.model.math.MathData;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.Triple;

import java.util.*;
import java.util.stream.Collectors;

public class TestModelData {
    public static void main(String[] args) {
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
