package com.betsoft.casino.mp.clashofthegods.model.math.enemies;
import com.betsoft.casino.mp.clashofthegods.model.math.WeaponData;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.dgphoenix.casino.common.util.Pair;
import java.util.Collections;
import static com.betsoft.casino.mp.clashofthegods.model.math.MathData.*;


public class Phoenix extends AbstractEnemyData {
    public Phoenix() {
        levels = new int[]{2000, 2500, 3500};

        weaponDropData.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(250.00, 1048.1)); // pistol
        weaponDropData.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(270.00, 1044.6)); // default data for enemy`
        additionalWeaponKilledTable.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t2\n" +
                "Napalm\t1\t\t\t2\n" +
                "Artillery\t1\t\t\t3\n" +
                "Nuke\t1\t\t\t4\n"));
        additionalWeaponKilledTable.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t2\n" +
                "Napalm\t1\t\t\t3\n" +
                "Artillery\t1\t\t\t4\n" +
                "Nuke\t1\t\t\t4\n"));

        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 9.0), 50.0, 100,
                                getHP("0\t\t52.00%\n" +
                                        "50\t\t25.00%\n" +
                                        "100\t\t10.00%\n" +
                                        "150\t\t5.00%\n" +
                                        "180\t\t4.00%\n" +
                                        "220\t\t2.00%\n" +
                                        "250\t\t1.00%\n" +
                                        "300\t\t1.00%\n"),
                                getHP("0\t\t86.54%\n" +
                                        "350.0\t\t13.46%\n")),
                        new WeaponData(Collections.singletonMap(2, 9.0), 50.0, 120,
                                getHP("0\t\t47.00%\n" +
                                        "50\t\t15.00%\n" +
                                        "100\t\t25.00%\n" +
                                        "150\t\t5.00%\n" +
                                        "180\t\t4.00%\n" +
                                        "220\t\t2.00%\n" +
                                        "250\t\t1.00%\n" +
                                        "300\t\t1.00%\n"),
                                getHP("0\t\t85.725%\n" +
                                        "400.0\t\t14.275%\n")),
                        new WeaponData(Collections.singletonMap(2, 9.0), 50.0, 140,
                                getHP("0\t\t29.00%\n" +
                                        "50\t\t20.00%\n" +
                                        "100\t\t25.00%\n" +
                                        "150\t\t15.00%\n" +
                                        "180\t\t5.00%\n" +
                                        "220\t\t4.00%\n" +
                                        "250\t\t1.00%\n" +
                                        "300\t\t1.00%\n"),
                                getHP("0\t\t79.80%\n" +
                                        "400.0\t\t20.20%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        50, 30,
                        getHP("0\t\t18.00%\n" +
                                "40\t\t20.00%\n" +
                                "60\t\t35.00%\n" +
                                "80\t\t10.00%\n" +
                                "100\t\t8.00%\n" +
                                "150\t\t4.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t2.00%\n"),
                        getHP("0\t\t80.00%\n" +
                                "310.0\t\t20.00%\n"))});
        freeWeaponData.put(SpecialWeaponType.Lightning.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 40,
                        getHP("0\t\t9.00%\n" +
                                "40\t\t20.00%\n" +
                                "60\t\t35.00%\n" +
                                "80\t\t15.00%\n" +
                                "100\t\t12.00%\n" +
                                "150\t\t5.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t2.00%\n"),
                        getHP("0\t\t78.28%\n" +
                                "320.0\t\t21.72%\n"))});

        freeWeaponData.put(SpecialWeaponType.Napalm.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 30,
                        getHP("0\t\t10.00%\n" +
                                "40\t\t20.00%\n" +
                                "60\t\t35.00%\n" +
                                "80\t\t15.00%\n" +
                                "100\t\t12.00%\n" +
                                "150\t\t5.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t1.00%\n"),
                        getHP("0\t\t78.39%\n" +
                                "310.0\t\t21.61%\n"))});

        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 120,
                        getHP("0\t\t36.25%\n" +
                                "300\t\t35.00%\n" +
                                "400\t\t20.00%\n" +
                                "500\t\t5.00%\n" +
                                "750\t\t2.00%\n" +
                                "1500\t\t1.00%\n" +
                                "2500\t\t0.50%\n" +
                                "5000\t\t0.25%\n"),
                        getHP("0\t\t64.67%\n" +
                                "750.0\t\t35.33%\n"))});
        freeWeaponData.put(SpecialWeaponType.Nuke.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 100,
                        getHP("0\t\t15.00%\n" +
                                "150\t\t25.00%\n" +
                                "250\t\t35.00%\n" +
                                "500\t\t20.00%\n" +
                                "750\t\t2.00%\n" +
                                "1000\t\t1.00%\n" +
                                "1500\t\t1.00%\n" +
                                "2500\t\t1.00%\n"),
                        getHP("0\t\t42.00%\n" +
                                "500.0\t\t58.00%\n"))});
    }
}
