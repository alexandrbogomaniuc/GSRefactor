package com.betsoft.casino.mp.clashofthegods.model.math.enemies;

import com.betsoft.casino.mp.clashofthegods.model.math.WeaponData;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.abs.casino.common.util.Pair;
import java.util.Collections;
import static com.betsoft.casino.mp.clashofthegods.model.math.MathData.*;
import static com.betsoft.casino.mp.clashofthegods.model.math.MathData.getHP;

public class GoldenDragon extends AbstractEnemyData {

    public GoldenDragon() {
        levels = new int[]{2500, 3000, 3500};

        weaponDropData.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(150.00, 1060.3)); // pistol
        weaponDropData.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(200.00, 1267.5)); // default data for enemy
        additionalWeaponKilledTable.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t1\n" +
                "Napalm\t1\t\t\t1\n" +
                "Artillery\t1\t\t\t2\n" +
                "Nuke\t1\t\t\t3\n"));
        additionalWeaponKilledTable.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t0\n" +
                "Lightning\t1\t\t\t0\n" +
                "Napalm\t1\t\t\t1\n" +
                "Artillery\t1\t\t\t4\n" +
                "Nuke\t1\t\t\t4\n"));

        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 9.0), 50.0, 100,
                                getHP("0\t\t21.00%\n" +
                                        "40\t\t25.00%\n" +
                                        "80\t\t30.00%\n" +
                                        "120\t\t15.00%\n" +
                                        "160\t\t5.00%\n" +
                                        "200\t\t2.00%\n" +
                                        "250\t\t1.00%\n" +
                                        "300\t\t1.00%\n"),
                                getHP("0\t\t80.14%\n" +
                                        "350.0\t\t19.86%\n")),
                        new WeaponData(Collections.singletonMap(2, 9.0), 50.0, 120,
                                getHP("0\t\t19.00%\n" +
                                        "40\t\t15.00%\n" +
                                        "80\t\t25.00%\n" +
                                        "120\t\t25.00%\n" +
                                        "160\t\t12.00%\n" +
                                        "200\t\t2.00%\n" +
                                        "250\t\t1.00%\n" +
                                        "300\t\t1.00%\n"),
                                getHP("0\t\t78.825%\n" +
                                        "400.0\t\t21.175%\n")),
                        new WeaponData(Collections.singletonMap(2, 9.0), 50.0, 140,
                                getHP("0\t\t9.00%\n" +
                                        "40\t\t15.00%\n" +
                                        "80\t\t30.00%\n" +
                                        "120\t\t27.00%\n" +
                                        "160\t\t10.00%\n" +
                                        "200\t\t6.00%\n" +
                                        "250\t\t2.00%\n" +
                                        "300\t\t1.00%\n"),
                                getHP("0\t\t75.40%\n" +
                                        "400.0\t\t24.60%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        50, 30,
                        getHP("0\t\t3.00%\n" +
                                "40\t\t40.00%\n" +
                                "60\t\t30.00%\n" +
                                "80\t\t10.00%\n" +
                                "100\t\t8.00%\n" +
                                "150\t\t4.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t2.00%\n"),
                        getHP("0\t\t76.07%\n" +
                                "280.0\t\t23.93%\n"))});
        freeWeaponData.put(SpecialWeaponType.Lightning.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 40,
                        getHP("0\t\t4.00%\n" +
                                "40\t\t35.00%\n" +
                                "60\t\t25.00%\n" +
                                "80\t\t15.00%\n" +
                                "100\t\t12.00%\n" +
                                "150\t\t5.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t2.00%\n"),
                        getHP("0\t\t75.18%\n" +
                                "280.0\t\t24.82%\n"))});

        freeWeaponData.put(SpecialWeaponType.Napalm.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        45, 30,
                        getHP("0\t\t5.00%\n" +
                                "40\t\t30.00%\n" +
                                "60\t\t30.00%\n" +
                                "80\t\t15.00%\n" +
                                "100\t\t12.00%\n" +
                                "150\t\t5.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t1.00%\n"),
                        getHP("0\t\t77.33%\n" +
                                "300.0\t\t22.67%\n"))});

        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 120,
                        getHP("0\t\t1.25%\n" +
                                "300\t\t60.00%\n" +
                                "400\t\t30.00%\n" +
                                "500\t\t5.00%\n" +
                                "750\t\t2.00%\n" +
                                "1500\t\t1.00%\n" +
                                "2500\t\t0.50%\n" +
                                "5000\t\t0.25%\n"),
                        getHP("0\t\t49.33%\n" +
                                "750.0\t\t50.67%\n"))});
        freeWeaponData.put(SpecialWeaponType.Nuke.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 100,
                        getHP("0\t\t8.00%\n" +
                                "150\t\t32.00%\n" +
                                "250\t\t35.00%\n" +
                                "500\t\t20.00%\n" +
                                "750\t\t2.00%\n" +
                                "1000\t\t1.00%\n" +
                                "1500\t\t1.00%\n" +
                                "2500\t\t1.00%\n"),
                        getHP("0\t\t39.90%\n" +
                                "500.0\t\t60.10%\n"))});

    }
}
