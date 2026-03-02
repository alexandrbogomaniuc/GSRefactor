package com.betsoft.casino.mp.clashofthegods;

import com.betsoft.casino.mp.clashofthegods.model.math.EnemyType;
import com.betsoft.casino.mp.clashofthegods.model.math.MathData;
import com.betsoft.casino.mp.clashofthegods.model.math.WeaponData;
import com.betsoft.casino.mp.clashofthegods.model.math.enemies.IEnemyData;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.abs.casino.common.util.Pair;

import java.util.Comparator;
import java.util.Optional;

public class TestWrongWeapons {
    public static void main(String[] args) {
        int gameId = (int) GameType.CLASH_OF_THE_GODS.getGameId();
        for (EnemyType value : EnemyType.values()) {
            IEnemyData enemyData = MathData.getEnemyData(value.getId());
            if(enemyData.getLevels() == null)
                continue;
            for (int i = 0; i < enemyData.getLevels().length; i++) {
                WeaponData weaponData = enemyData.getWeaponDataMap(-1, i, false);
                Pair<Double, Double> weaponDropData = MathData.getWeaponDropData(value.getId(), -1);
                Double avgWeaponAward = weaponDropData == null ? 0. : weaponDropData.getKey();
                if (weaponData.getEnemyLowHitPointMap() != null) {
                    Optional<Long> max = weaponData.getEnemyLowHitPointMap().keySet().stream().max(Comparator.naturalOrder());
                    double diff = max.get() - avgWeaponAward;
                    if(diff < 0) System.out.println("-------------------------------------------------------------------------");
                    System.out.println(value.getName() + ", Pistol " + " level: " + i + " diff:" + diff + " max.get(): " + max.get() + " avgWeaponAward: " + avgWeaponAward);
                } else {
                    System.out.println(value.getName() + ", Pistol " + " level: " + i + " no low table");
                }
            }

            for (SpecialWeaponType specialWeaponType : SpecialWeaponType.values()) {
                if (specialWeaponType.getAvailableGameIds().contains(gameId)) {
                    for (int i = 0; i < enemyData.getLevels().length; i++) {
                        WeaponData weaponData = enemyData.getWeaponDataMap(specialWeaponType.getId(), i, false);
                        Pair<Double, Double> weaponDropData = MathData.getWeaponDropData(value.getId(), specialWeaponType.getId());
                        Double avgWeaponAward = weaponDropData == null ? 0. : weaponDropData.getKey();
                        if (weaponData.getEnemyLowHitPointMap() != null) {
                            Optional<Long> max = weaponData.getEnemyLowHitPointMap().keySet().stream().max(Comparator.naturalOrder());
                            double diff = max.get() - avgWeaponAward;
                            if(diff < 0) System.out.println("-------------------------------------------------------------------------");
                            System.out.println(value.getName() + ",  " + specialWeaponType.getTitle() + " level: " + i + " diff:" + diff + " max.get(): " + max.get() + " avgWeaponAward: " + avgWeaponAward);
                        } else {
                            System.out.println(value.getName() + ", " + specialWeaponType.getTitle() + " level: " + i + " no low table");
                        }
                    }
                }
            }
        }
    }
}
