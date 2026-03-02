package com.betsoft.casino.mp.clashofthegods.model.math.enemies;

import com.betsoft.casino.mp.clashofthegods.model.math.WeaponData;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.abs.casino.common.util.Pair;
import java.util.Collections;
import static com.betsoft.casino.mp.clashofthegods.model.math.MathData.*;

public class Snake extends AbstractEnemyData{

    public Snake() {
        levels = new int[]{350, 400, 500};

        weaponDropData.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(40.00, 767.8)); // pistol
        weaponDropData.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(80.00, 913.3)); // default data for enemy
        additionalWeaponKilledTable.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t2\n" +
                "Lightning\t1\t\t\t2\n" +
                "Napalm\t1\t\t\t2\n" +
                "Artillery\t1\t\t\t1\n" +
                "Nuke\t1\t\t\t1\n"));
        additionalWeaponKilledTable.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t1\n" +
                "Napalm\t1\t\t\t2\n" +
                "Artillery\t1\t\t\t2\n" +
                "Nuke\t1\t\t\t1\n"));

        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 5.0), 35.0, 30,
                                getHP("0\t\t40.00%\n" +
                                        "20\t\t30.00%\n" +
                                        "40\t\t20.00%\n" +
                                        "60\t\t10.00%\n"),
                                getHP("0\t\t66.67%\n" +
                                        "60.0\t\t33.33%\n")),
                        new WeaponData(Collections.singletonMap(2, 5.0), 30.0, 35,
                                getHP("0\t\t43.00%\n" +
                                        "30\t\t30.00%\n" +
                                        "50\t\t15.00%\n" +
                                        "70\t\t12.00%\n"),
                                getHP("0\t\t64.43%\n" +
                                        "70.0\t\t35.57%\n")),
                        new WeaponData(Collections.singletonMap(2, 5.0), 30.0, 40,
                                getHP("0\t\t40.00%\n" +
                                        "30\t\t30.00%\n" +
                                        "50\t\t20.00%\n" +
                                        "80\t\t10.00%\n"),
                                getHP("0\t\t66.25%\n" +
                                        "80.0\t\t33.75%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        45, 30,
                        getHP("0\t\t1.25%\n" +
                                "30\t\t75.00%\n" +
                                "40\t\t15.00%\n" +
                                "50\t\t5.00%\n" +
                                "70\t\t2.00%\n" +
                                "80\t\t1.00%\n" +
                                "100\t\t0.50%\n" +
                                "150\t\t0.25%\n"),
                        getHP("0\t\t65.925%\n" +
                                "100.0\t\t34.075%\n"))});
        freeWeaponData.put(SpecialWeaponType.Lightning.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 30,
                        getHP("0\t\t1.25%\n" +
                                "40\t\t75.00%\n" +
                                "50\t\t15.00%\n" +
                                "60\t\t5.00%\n" +
                                "70\t\t2.00%\n" +
                                "80\t\t1.00%\n" +
                                "100\t\t0.50%\n" +
                                "180\t\t0.25%\n"),
                        getHP("0\t\t60.32%\n" +
                                "110.0\t\t39.68%\n"))});

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
                        getHP("0\t\t65.92%\n" +
                                "120.0\t\t34.08%\n"))});

        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 70,
                        getHP("0\t\t0.25%\n" +
                                "100\t\t53.00%\n" +
                                "120\t\t30.00%\n" +
                                "150\t\t12.00%\n" +
                                "170\t\t3.00%\n" +
                                "200\t\t1.00%\n" +
                                "250\t\t0.50%\n" +
                                "500\t\t0.25%\n"),
                        getHP("0\t\t16.71%\n" +
                                "140.0\t\t83.29%\n"))});
        freeWeaponData.put(SpecialWeaponType.Nuke.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 40,
                        getHP("0\t\t3.50%\n" +
                                "100\t\t40.00%\n" +
                                "120\t\t40.00%\n" +
                                "150\t\t10.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t2.00%\n" +
                                "300\t\t1.00%\n" +
                                "500\t\t0.50%\n"),
                        getHP("0\t\t0.42%\n" +
                                "120.0\t\t99.58%\n"))});
    }
}
