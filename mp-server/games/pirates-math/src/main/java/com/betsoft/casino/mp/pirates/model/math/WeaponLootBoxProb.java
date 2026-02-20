package com.betsoft.casino.mp.pirates.model.math;

import com.betsoft.casino.mp.model.SpecialWeaponType;

import java.util.Arrays;
import java.util.List;

public class WeaponLootBoxProb {

//    special_1		100	125	150
//    special_2		100	120	140
//    grenade		25	30	35
//    mine_launcher		50	60	70
//    rocket_launcher		80	100	120
//    machine_gun		80	100	120
//    laser		50	60	70
//    flamethrower		18	20	22
//    cryogun		12	14	16
//    plasmagun		80	100	120
//    railgun		80	100	120
//    artillery_strike		8	10	12

    private static List<List<WeaponEntry>> configTables200;

    public static final List<List<WeaponEntry>> defaultTables200 = Arrays.asList(
            Arrays.asList(
                    new WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 20), //
                    new WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 22),
                    new WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 25),
                    new WeaponEntry(1, SpecialWeaponType.DoubleStrengthPowerUp.getId(), 27),

                    new WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 18),
                    new WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 20),
                    new WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 22),
                    new WeaponEntry(1, SpecialWeaponType.HolyArrows.getId(), 25),

                    new WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 6), // grenade
                    new WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 7),
                    new WeaponEntry(1, SpecialWeaponType.Bomb.getId(), 8),
                    new WeaponEntry(2, SpecialWeaponType.Bomb.getId(), 9), // grenade

                    new WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 5), // mine_launcher
                    new WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 6),
                    new WeaponEntry(1, SpecialWeaponType.Landmines.getId(), 7),
                    new WeaponEntry(2, SpecialWeaponType.Landmines.getId(), 8),

                    new WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 8), // rocket_launcher
                    new WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 9),
                    new WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 10),
                    new WeaponEntry(1, SpecialWeaponType.RocketLauncher.getId(), 12),


                    new WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 8), //  machine_gun
                    new WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 9),
                    new WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 10),
                    new WeaponEntry(1, SpecialWeaponType.MachineGun.getId(), 12),


                    new WeaponEntry(2, SpecialWeaponType.Ricochet.getId(), 4), // laser
                    new WeaponEntry(2, SpecialWeaponType.Ricochet.getId(), 5),
                    new WeaponEntry(2, SpecialWeaponType.Ricochet.getId(), 6),
                    new WeaponEntry(1, SpecialWeaponType.Ricochet.getId(), 7),


                    new WeaponEntry(2, SpecialWeaponType.Flamethrower.getId(), 3), // flamethrower
                    new WeaponEntry(2, SpecialWeaponType.Flamethrower.getId(), 4),
                    new WeaponEntry(2, SpecialWeaponType.Flamethrower.getId(), 5),
                    new WeaponEntry(1, SpecialWeaponType.Flamethrower.getId(), 6),


                    new WeaponEntry(2, SpecialWeaponType.Cryogun.getId(), 3), // cryogun
                    new WeaponEntry(2, SpecialWeaponType.Cryogun.getId(), 4),
                    new WeaponEntry(2, SpecialWeaponType.Cryogun.getId(), 5),
                    new WeaponEntry(1, SpecialWeaponType.Cryogun.getId(), 6),


                    new WeaponEntry(1, SpecialWeaponType.Plasma.getId(), 4), // plasmagun
                    new WeaponEntry(2, SpecialWeaponType.Plasma.getId(), 5),
                    new WeaponEntry(2, SpecialWeaponType.Plasma.getId(), 6),
                    new WeaponEntry(2, SpecialWeaponType.Plasma.getId(), 7),

                    new WeaponEntry(1, SpecialWeaponType.Railgun.getId(), 4), // railgun
                    new WeaponEntry(2, SpecialWeaponType.Railgun.getId(), 5),
                    new WeaponEntry(2, SpecialWeaponType.Railgun.getId(), 6),
                    new WeaponEntry(2, SpecialWeaponType.Railgun.getId(), 7),

                    new WeaponEntry(2, SpecialWeaponType.ArtilleryStrike.getId(), 3), // artillery_strike
                    new WeaponEntry(2, SpecialWeaponType.ArtilleryStrike.getId(), 4),
                    new WeaponEntry(2, SpecialWeaponType.ArtilleryStrike.getId(), 5),
                    new WeaponEntry(1, SpecialWeaponType.ArtilleryStrike.getId(), 6)
            ));

    public static List<List<WeaponEntry>> getTables200() {
        return configTables200 == null ? defaultTables200 : configTables200;
    }

    public static void setConfigTables200(List<List<WeaponEntry>> configTables200) {
        WeaponLootBoxProb.configTables200 = configTables200;
    }

    public static class WeaponEntry {
        private int weight;
        private int type;
        private int shots;

        public WeaponEntry(int weight, int type, int shots) {
            this.weight = weight;
            this.type = type;
            this.shots = shots;
        }

        public int getWeight() {
            return weight;
        }

        public int getType() {
            return type;
        }

        public int getShots() {
            return shots;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("WeaponEntry [");
            sb.append("weight=").append(weight);
            sb.append(", type=").append(type);
            sb.append(", shots=").append(shots);
            sb.append(']');
            return sb.toString();
        }
    }
}
