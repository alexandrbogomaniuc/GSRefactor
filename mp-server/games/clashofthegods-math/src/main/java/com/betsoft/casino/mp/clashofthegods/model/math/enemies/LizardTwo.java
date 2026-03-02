package com.betsoft.casino.mp.clashofthegods.model.math.enemies;

import com.betsoft.casino.mp.clashofthegods.model.math.WeaponData;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.abs.casino.common.util.Pair;
import java.util.Collections;

import static com.betsoft.casino.mp.clashofthegods.model.math.MathData.*;
import static com.betsoft.casino.mp.clashofthegods.model.math.MathData.getHP;

public class LizardTwo extends AbstractEnemyData {
    public LizardTwo() {
        levels = new int[]{400, 550, 800};

        weaponDropData.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(120.00, 877.5)); // pistol
        weaponDropData.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(180.00, 1002.9)); // default data for enemy
        additionalWeaponKilledTable.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t1\n" +
                "Napalm\t1\t\t\t1\n" +
                "Artillery\t1\t\t\t1\n" +
                "Nuke\t1\t\t\t1\n"));
        additionalWeaponKilledTable.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t1\n" +
                "Napalm\t1\t\t\t1\n" +
                "Artillery\t1\t\t\t2\n" +
                "Nuke\t1\t\t\t2\n"));

        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 6.0), 40.0, 70,
                                getHP("0\t\t4.00%\n" +
                                        "50\t\t60.00%\n" +
                                        "80\t\t30.00%\n" +
                                        "120\t\t5.00%\n" +
                                        "160\t\t1.00%\n"),
                                getHP("0\t\t69.20%\n" +
                                        "200.0\t\t30.80%\n")),
                        new WeaponData(Collections.singletonMap(2, 6.0), 35.0, 80,
                                getHP("0\t\t0.00%\n" +
                                        "50\t\t72.00%\n" +
                                        "80\t\t20.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "130\t\t2.00%\n" +
                                        "160\t\t1.00%\n"),
                                getHP("0\t\t72.18%\n" +
                                        "220.0\t\t27.82%\n")),
                        new WeaponData(Collections.singletonMap(2, 6.0), 35.0, 90,
                                getHP("0\t\t2.00%\n" +
                                        "50\t\t75.00%\n" +
                                        "80\t\t12.00%\n" +
                                        "100\t\t8.00%\n" +
                                        "150\t\t2.00%\n" +
                                        "200\t\t1.00%\n"),
                                getHP("0\t\t73.87%\n" +
                                        "230.0\t\t26.13%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        45, 30,
                        getHP("0\t\t1.00%\n" +
                                "40\t\t46.00%\n" +
                                "50\t\t30.00%\n" +
                                "60\t\t10.00%\n" +
                                "70\t\t5.00%\n" +
                                "80\t\t3.00%\n" +
                                "120\t\t3.00%\n" +
                                "200\t\t2.00%\n"),
                        getHP("0\t\t75.95%\n" +
                                "220.0\t\t24.05%\n"))});
        freeWeaponData.put(SpecialWeaponType.Lightning.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 20,
                        getHP("0\t\t2.00%\n" +
                                "50\t\t27.00%\n" +
                                "60\t\t35.00%\n" +
                                "70\t\t23.00%\n" +
                                "90\t\t6.00%\n" +
                                "100\t\t4.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t1.00%\n"),
                        getHP("0\t\t69.77%\n" +
                                "220.0\t\t30.23%\n"))});

        freeWeaponData.put(SpecialWeaponType.Napalm.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        40, 30,
                        getHP("0\t\t2.00%\n" +
                                "40\t\t50.00%\n" +
                                "50\t\t20.00%\n" +
                                "60\t\t10.00%\n" +
                                "90\t\t8.00%\n" +
                                "120\t\t5.00%\n" +
                                "200\t\t3.00%\n" +
                                "300\t\t2.00%\n"),
                        getHP("0\t\t72.18%\n" +
                                "220.0\t\t27.82%\n"))});

        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 80,
                        getHP("0\t\t3.50%\n" +
                                "100\t\t20.00%\n" +
                                "120\t\t50.00%\n" +
                                "150\t\t16.00%\n" +
                                "200\t\t7.00%\n" +
                                "250\t\t2.00%\n" +
                                "340\t\t1.00%\n" +
                                "500\t\t0.50%\n"),
                        getHP("0\t\t57.03%\n" +
                                "300.0\t\t42.97%\n"))});
        freeWeaponData.put(SpecialWeaponType.Nuke.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 50,
                        getHP("0\t\t0.00%\n" +
                                "100\t\t40.00%\n" +
                                "120\t\t40.00%\n" +
                                "150\t\t12.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t2.00%\n" +
                                "500\t\t2.00%\n" +
                                "750\t\t1.00%\n"),
                        getHP("0\t\t55.17%\n" +
                                "300.0\t\t44.83%\n"))});

    }
}
