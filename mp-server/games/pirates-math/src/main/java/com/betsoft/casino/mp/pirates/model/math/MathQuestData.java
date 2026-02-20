package com.betsoft.casino.mp.pirates.model.math;

import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.dgphoenix.casino.common.util.Pair;
import com.google.common.util.concurrent.AtomicDouble;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MathQuestData {

    public static final Map<Integer, Pair<Double, Double>> questsRTPAndCacheWins;
    public static final Map<Integer, Double> questsSpecialWeaponsAverageWins;

    public static final List<List<WeaponLootBoxProb.WeaponEntry>> quest = Arrays.asList(
            Arrays.asList(
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 20), // special_1
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 22),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 25),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 27),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 18), //special_2
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 20),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 22),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 25),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 6), // grenade
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 7),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 8),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 9),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 5), // mine_launcher
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 6),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 7),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 8),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 8), // rocket_launcher
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 9),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 10),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 12),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 8), //  machine_gun
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 9),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 10),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 12),

                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Ricochet.getId(), 4), // laser
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Ricochet.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Ricochet.getId(), 6),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Ricochet.getId(), 7),

                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Flamethrower.getId(), 3), // flamethrower
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Flamethrower.getId(), 4),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Flamethrower.getId(), 5),
//                    new WeaponLootBoxProb.WeaponEntry(0, SpecialWeaponType.Flamethrower.getId(), 6),

                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Cryogun.getId(), 3), // cryogun
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Cryogun.getId(), 4),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Cryogun.getId(), 5),
//                    new WeaponLootBoxProb.WeaponEntry(0, SpecialWeaponType.Cryogun.getId(), 6),

                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Plasma.getId(), 4), // plasmagun
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Plasma.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Plasma.getId(), 6),
//                    new WeaponLootBoxProb.WeaponEntry(0, SpecialWeaponType.Plasma.getId(), 7),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Railgun.getId(), 4), // railgun
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Railgun.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Railgun.getId(), 6),
//                    new WeaponLootBoxProb.WeaponEntry(0, SpecialWeaponType.Railgun.getId(), 7),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.ArtilleryStrike.getId(), 3), // artillery_strike
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.ArtilleryStrike.getId(), 4),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.ArtilleryStrike.getId(), 5)
//                    new WeaponLootBoxProb.WeaponEntry(0, SpecialWeaponType.ArtilleryStrike.getId(), 6)
            ),
            Arrays.asList(
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 20), // special_1
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 22),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 25),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 27),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 18), //special_2
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 20),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 22),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 25),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 6), // grenade
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 7),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 8),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 9),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 5), // mine_launcher
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 6),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 7),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 8),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 8), // rocket_launcher
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 9),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 10),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 12),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 8), //  machine_gun
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 9),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 10),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 12),

                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Ricochet.getId(), 4), // laser
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Ricochet.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Ricochet.getId(), 6),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Ricochet.getId(), 7),

                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Flamethrower.getId(), 3), // flamethrower
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Flamethrower.getId(), 4),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Flamethrower.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Flamethrower.getId(), 6),

                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Cryogun.getId(), 3), // cryogun
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Cryogun.getId(), 4),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Cryogun.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Cryogun.getId(), 6),

                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Plasma.getId(), 4), // plasmagun
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Plasma.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Plasma.getId(), 6),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Plasma.getId(), 7),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Railgun.getId(), 4), // railgun
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Railgun.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Railgun.getId(), 6),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Railgun.getId(), 7),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.ArtilleryStrike.getId(), 3), // artillery_strike
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.ArtilleryStrike.getId(), 4),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.ArtilleryStrike.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.ArtilleryStrike.getId(), 6)
            ),

            Arrays.asList(
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 20), // special_1
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 22),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 25),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 27),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 18), //special_2
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.HolyArrows.getId(), 20),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 22),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 25),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 6), // grenade
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 7),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 8),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Bomb.getId(), 9),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 5), // mine_launcher
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 6),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 7),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Landmines.getId(), 8),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 8), // rocket_launcher
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 9),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 10),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.RocketLauncher.getId(), 12),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 8), //  machine_gun
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 9),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 10),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.MachineGun.getId(), 12),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Ricochet.getId(), 4), // laser
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Ricochet.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Ricochet.getId(), 6),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Ricochet.getId(), 7),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Flamethrower.getId(), 3), // flamethrower
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Flamethrower.getId(), 4),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Flamethrower.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Flamethrower.getId(), 6),

                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Cryogun.getId(), 3), // cryogun
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Cryogun.getId(), 4),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Cryogun.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Cryogun.getId(), 6),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Plasma.getId(), 4), // plasmagun
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Plasma.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Plasma.getId(), 6),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Plasma.getId(), 7),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Railgun.getId(), 4), // railgun
                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.Railgun.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Railgun.getId(), 6),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.Railgun.getId(), 7),

                    new WeaponLootBoxProb.WeaponEntry(1, SpecialWeaponType.ArtilleryStrike.getId(), 3), // artillery_strike
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.ArtilleryStrike.getId(), 4),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.ArtilleryStrike.getId(), 5),
                    new WeaponLootBoxProb.WeaponEntry(2, SpecialWeaponType.ArtilleryStrike.getId(), 6)
            )
    );

    static {
        questsRTPAndCacheWins = new HashMap<>();
        questsRTPAndCacheWins.put(Treasure.BRONZE_CHEST.id, new Pair<Double, Double>(2.50, 20.));
        questsRTPAndCacheWins.put(Treasure.SILVER_CHEST.id, new Pair<Double, Double>(3.00, 50.));
        questsRTPAndCacheWins.put(Treasure.GOLD_CHEST.id, new Pair<Double, Double>(4.50, 150.));

        questsSpecialWeaponsAverageWins = new HashMap<>();

        for (Treasure treasure : Treasure.values()) {
            List<WeaponLootBoxProb.WeaponEntry> weaponEntries = quest.get(treasure.getId() - 1);
            AtomicInteger sum = new AtomicInteger(0);
            weaponEntries.forEach(weaponEntry -> sum.addAndGet(weaponEntry.getWeight()));
            AtomicDouble totalEV = new AtomicDouble(0);
            weaponEntries.forEach(weaponEntry -> {
                int shots = weaponEntry.getShots();
                int type = weaponEntry.getType();
                double prob = (double) weaponEntry.getWeight() / sum.get();
                Double averageDamageForWeapon = MathData.getAverageDamageForWeapon(type);
                Double rtpForWeapon = MathData.getRtpForWeapon(type) / 100;
                double ev = shots * averageDamageForWeapon * rtpForWeapon;
                totalEV.addAndGet(ev * prob);
            });
            questsSpecialWeaponsAverageWins.put(treasure.getId(), totalEV.doubleValue());
        }
    }

    public static double getProbabilityOkKey(int keyId) {
        Pair<Double, Double> pair = questsRTPAndCacheWins.get(keyId);
        Double rtp = pair.getKey();
        Double cachePart = pair.getValue();
        Double weaponPart = questsSpecialWeaponsAverageWins.get(keyId);
        double probQuest = rtp / (cachePart + weaponPart);
        return probQuest * 3 / 100;
    }


    public static Pair<Integer, Integer> getRandomWeapon(int treasureId) {
        List<WeaponLootBoxProb.WeaponEntry> weaponEntries = quest.get(treasureId - 1);
        AtomicInteger sum = new AtomicInteger(0);
        weaponEntries.forEach(weaponEntry -> sum.addAndGet(weaponEntry.getWeight()));

        double[] prob = new double[weaponEntries.size()];
        for (int i = 0; i < prob.length; i++) {
            prob[i] = (double) weaponEntries.get(i).getWeight() / sum.get();
        }
        int indexFromDoubleProb = GameTools.getIndexFromDoubleProb(prob);
        WeaponLootBoxProb.WeaponEntry weaponEntry = weaponEntries.get(indexFromDoubleProb);
        return new Pair<>(weaponEntry.getType(), weaponEntry.getShots());
    }
}
