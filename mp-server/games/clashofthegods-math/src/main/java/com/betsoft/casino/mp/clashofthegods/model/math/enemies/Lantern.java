package com.betsoft.casino.mp.clashofthegods.model.math.enemies;

import com.betsoft.casino.mp.clashofthegods.model.math.WeaponData;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.dgphoenix.casino.common.util.Pair;
import java.util.Collections;

import static com.betsoft.casino.mp.clashofthegods.model.math.MathData.*;
import static com.betsoft.casino.mp.clashofthegods.model.math.MathData.getHP;

public class Lantern extends AbstractEnemyData {
    public Lantern() {
        levels = new int[]{100, 180, 250};

        weaponDropData.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(30.00, 877.5)); // pistol
        weaponDropData.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(100.00, 987.2)); // default data for enemy
        additionalWeaponKilledTable.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t2\n" +
                "Lightning\t1\t\t\t2\n" +
                "Napalm\t1\t\t\t2\n" +
                "Artillery\t1\t\t\t2\n" +
                "Nuke\t1\t\t\t2\n"));
        additionalWeaponKilledTable.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t1\n" +
                "Napalm\t1\t\t\t2\n" +
                "Artillery\t1\t\t\t2\n" +
                "Nuke\t1\t\t\t2\n"));

        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 8.0), 35.0, 30,
                                getHP("0\t\t40.00%\n" +
                                        "20\t\t30.00%\n" +
                                        "40\t\t20.00%\n" +
                                        "60\t\t10.00%\n"),
                                getHP("0\t\t71.43%\n" +
                                        "70.0\t\t28.57%\n")),
                        new WeaponData(Collections.singletonMap(2, 8.0), 35.0, 35,
                                getHP("0\t\t43.00%\n" +
                                        "30\t\t30.00%\n" +
                                        "50\t\t15.00%\n" +
                                        "70\t\t12.00%\n"),
                                getHP("0\t\t68.875%\n" +
                                        "80.0\t\t31.125%\n")),
                        new WeaponData(Collections.singletonMap(2, 8.0), 35.0, 40,
                                getHP("0\t\t40.00%\n" +
                                        "30\t\t30.00%\n" +
                                        "50\t\t20.00%\n" +
                                        "80\t\t10.00%\n"),
                                getHP("0\t\t70.00%\n" +
                                        "90.0\t\t30.00%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        35, 30,
                        getHP("0\t\t1.25%\n" +
                                "30\t\t75.00%\n" +
                                "40\t\t15.00%\n" +
                                "50\t\t5.00%\n" +
                                "70\t\t2.00%\n" +
                                "80\t\t1.00%\n" +
                                "100\t\t0.50%\n" +
                                "150\t\t0.25%\n"),
                        getHP("0\t\t73.79%\n" +
                                "130.0\t\t26.21%\n"))});
        freeWeaponData.put(SpecialWeaponType.Lightning.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        35, 30,
                        getHP("0\t\t1.25%\n" +
                                "40\t\t75.00%\n" +
                                "50\t\t15.00%\n" +
                                "60\t\t5.00%\n" +
                                "70\t\t2.00%\n" +
                                "80\t\t1.00%\n" +
                                "100\t\t0.50%\n" +
                                "180\t\t0.25%\n"),
                        getHP("0\t\t68.82%\n" +
                                "140.0\t\t31.18%\n"))});

        freeWeaponData.put(SpecialWeaponType.Napalm.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        35, 30,
                        getHP("0\t\t4.50%\n" +
                                "30\t\t55.00%\n" +
                                "50\t\t21.00%\n" +
                                "60\t\t10.00%\n" +
                                "70\t\t6.00%\n" +
                                "80\t\t2.00%\n" +
                                "120\t\t1.00%\n" +
                                "180\t\t0.50%\n"),
                        getHP("0\t\t72.73%\n" +
                                "150.0\t\t27.27%\n"))});

        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        35, 70,
                        getHP("0\t\t0.25%\n" +
                                "100\t\t53.00%\n" +
                                "120\t\t30.00%\n" +
                                "150\t\t12.00%\n" +
                                "170\t\t3.00%\n" +
                                "200\t\t1.00%\n" +
                                "250\t\t0.50%\n" +
                                "500\t\t0.25%\n"),
                        getHP("0\t\t35.22%\n" +
                                "180.0\t\t64.78%\n"))});
        freeWeaponData.put(SpecialWeaponType.Nuke.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        35, 40,
                        getHP("0\t\t3.50%\n" +
                                "100\t\t40.00%\n" +
                                "120\t\t40.00%\n" +
                                "150\t\t10.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t2.00%\n" +
                                "300\t\t1.00%\n" +
                                "500\t\t0.50%\n"),
                        getHP("0\t\t20.33%\n" +
                                "150.0\t\t79.67%\n"))});

    }
}
