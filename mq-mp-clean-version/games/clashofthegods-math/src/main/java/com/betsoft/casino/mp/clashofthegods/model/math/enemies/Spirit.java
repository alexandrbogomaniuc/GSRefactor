package com.betsoft.casino.mp.clashofthegods.model.math.enemies;

import com.betsoft.casino.mp.clashofthegods.model.math.WeaponData;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import java.util.Collections;
import static com.betsoft.casino.mp.clashofthegods.model.math.MathData.*;

public class Spirit extends AbstractEnemyData{
    public Spirit() {
        levels = new int[]{400, 450, 500};

        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.EMPTY_MAP, 0.0, 50,
                                getHP("0\t\t10.00%\n" +
                                        "10\t\t60.00%\n" +
                                        "20\t\t20.00%\n" +
                                        "50\t\t10.00%\n"),
                                getHP("0\t\t94.55%\n" +
                                        "275.0\t\t5.45%\n")),
                        new WeaponData(Collections.EMPTY_MAP, 0.0, 60,
                                getHP("0\t\t8.00%\n" +
                                        "10\t\t65.00%\n" +
                                        "20\t\t15.00%\n" +
                                        "60\t\t12.00%\n"),
                                getHP("0\t\t94.43%\n" +
                                        "300.0\t\t5.57%\n")),
                        new WeaponData(Collections.EMPTY_MAP, 0.0, 70,
                                getHP("0\t\t15.00%\n" +
                                        "10\t\t55.00%\n" +
                                        "20\t\t20.00%\n" +
                                        "70\t\t10.00%\n"),
                                getHP("0\t\t94.92%\n" +
                                        "325.0\t\t5.08%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        0, 80,
                        getHP("0\t\t1.00%\n" +
                                "30\t\t75.00%\n" +
                                "40\t\t15.00%\n" +
                                "50\t\t5.00%\n" +
                                "70\t\t3.00%\n" +
                                "80\t\t1.00%\n"),
                        getHP("0\t\t89.06%\n" +
                                "310.0\t\t10.94%\n"))});
        freeWeaponData.put(SpecialWeaponType.Lightning.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        0, 80,
                        getHP("0\t\t0.00%\n" +
                                "30\t\t60.00%\n" +
                                "50\t\t22.00%\n" +
                                "60\t\t10.00%\n" +
                                "70\t\t7.00%\n" +
                                "80\t\t1.00%\n"),
                        getHP("0\t\t87.28%\n" +
                                "320.0\t\t12.72%\n"))});

        freeWeaponData.put(SpecialWeaponType.Napalm.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        0, 120,
                        getHP("0\t\t5.00%\n" +
                                "30\t\t55.00%\n" +
                                "50\t\t21.00%\n" +
                                "60\t\t10.00%\n" +
                                "80\t\t6.00%\n" +
                                "100\t\t2.00%\n" +
                                "120\t\t1.00%\n"),
                        getHP("0\t\t88.29%\n" +
                                "350.0\t\t11.71%\n"))});

        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        0, 150,
                        getHP("0\t\t0.50%\n" +
                                "50\t\t53.00%\n" +
                                "70\t\t30.00%\n" +
                                "80\t\t12.00%\n" +
                                "100\t\t3.00%\n" +
                                "120\t\t1.00%\n" +
                                "150\t\t0.50%\n"),
                        getHP("0\t\t84.49%\n" +
                                "400.0\t\t15.51%\n"))});
        freeWeaponData.put(SpecialWeaponType.Nuke.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        0, 170,
                        getHP("0\t\t5.00%\n" +
                                "50\t\t15.00%\n" +
                                "70\t\t40.00%\n" +
                                "100\t\t30.00%\n" +
                                "120\t\t7.00%\n" +
                                "150\t\t2.00%\n" +
                                "170\t\t1.00%\n"),
                        getHP("0\t\t80.35%\n" +
                                "400.0\t\t19.65%\n"))});
    }
}
