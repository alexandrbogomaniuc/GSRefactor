package com.betsoft.casino.mp.clashofthegods.model.math.enemies;

import com.betsoft.casino.mp.clashofthegods.model.math.WeaponData;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.dgphoenix.casino.common.util.Pair;
import java.util.Collections;

import static com.betsoft.casino.mp.clashofthegods.model.math.MathData.*;
import static com.betsoft.casino.mp.clashofthegods.model.math.MathData.getHP;

public class EvilSpiritOne extends AbstractEnemyData {
    public EvilSpiritOne() {
        levels = new int[]{120, 150, 180};

        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 4.0), 20.0, 0,
                                getHP("0\t\t20.00%\n" +
                                        "10\t\t40.00%\n" +
                                        "20\t\t30.00%\n" +
                                        "40\t\t10.00%\n"),
                                null),
                        new WeaponData(Collections.singletonMap(2, 4.0), 15.0, 0,
                                getHP("0\t\t20.00%\n" +
                                        "15\t\t40.00%\n" +
                                        "25\t\t30.00%\n" +
                                        "50\t\t10.00%\n"),
                                null),
                        new WeaponData(Collections.singletonMap(2, 4.0), 12.0, 0,
                                getHP("0\t\t15.00%\n" +
                                        "20\t\t50.00%\n" +
                                        "30\t\t25.00%\n" +
                                        "55\t\t10.00%\n"),
                                null)
                });
        freeWeaponData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        30, 0,
                        getHP("0\t\t13.00%\n" +
                                "30\t\t64.00%\n" +
                                "50\t\t20.00%\n" +
                                "60\t\t2.00%\n" +
                                "80\t\t1.00%\n"),
                        null)});
        freeWeaponData.put(SpecialWeaponType.Lightning.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 0,
                        getHP("0\t\t2.00%\n" +
                                "30\t\t63.00%\n" +
                                "40\t\t25.00%\n" +
                                "60\t\t6.00%\n" +
                                "70\t\t3.00%\n" +
                                "80\t\t1.00%\n"),
                        null)});

        freeWeaponData.put(SpecialWeaponType.Napalm.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        20, 0,
                        getHP("0\t\t3.00%\n" +
                                "30\t\t48.00%\n" +
                                "40\t\t15.00%\n" +
                                "50\t\t22.00%\n" +
                                "70\t\t6.00%\n" +
                                "80\t\t5.00%\n" +
                                "100\t\t1.00%\n"),
                        null)});

        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 0,
                        getHP("0\t\t0.00%\n" +
                                "40\t\t77.00%\n" +
                                "50\t\t15.00%\n" +
                                "70\t\t5.00%\n" +
                                "100\t\t1.50%\n" +
                                "120\t\t1.00%\n" +
                                "150\t\t0.50%\n"),
                        null)});
        freeWeaponData.put(SpecialWeaponType.Nuke.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        40, 0,
                        getHP("0\t\t0.50%\n" +
                                "40\t\t51.00%\n" +
                                "50\t\t35.00%\n" +
                                "60\t\t10.00%\n" +
                                "80\t\t2.00%\n" +
                                "100\t\t0.75%\n" +
                                "150\t\t0.50%\n" +
                                "200\t\t0.25%\n"),
                        null)});

    }
}
