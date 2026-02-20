package com.betsoft.casino.mp.clashofthegods.model.math.enemies;

import com.betsoft.casino.mp.clashofthegods.model.math.WeaponData;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import java.util.Collections;
import static com.betsoft.casino.mp.clashofthegods.model.math.MathData.*;

public class Boss extends AbstractEnemyData{
    public Boss() {

        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID,
                new WeaponData[]{
                        new WeaponData(getMapFromString("2=4.0"),
                                0, 1250, 500,
                                getHP("0\t\t15.00%\n" +
                                        "15\t\t40.00%\n" +
                                        "20\t\t25.00%\n" +
                                        "30\t\t15.00%\n" +
                                        "50\t\t5.00%\n"),
                                getHP("0\t\t79.48%\n" +
                                        "50\t\t8.00%\n" +
                                        "80\t\t6.00%\n" +
                                        "100\t\t2.00%\n" +
                                        "120\t\t2.00%\n" +
                                        "150\t\t1.00%\n" +
                                        "200\t\t1.00%\n" +
                                        "250\t\t0.52%\n"),
                                getHP("0\t\t97.60%\n" +
                                        "750.0\t\t2.40%\n")
                        ),
                        new WeaponData(getMapFromString("2=5.0"),
                                0, 2500, 600,
                                getHP("0\t\t10.00%\n" +
                                        "20\t\t40.00%\n" +
                                        "30\t\t25.00%\n" +
                                        "40\t\t15.00%\n" +
                                        "50\t\t5.00%\n" +
                                        "70\t\t5.00%\n"),
                                getHP("0\t\t79.81%\n" +
                                        "80\t\t8.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "120\t\t2.00%\n" +
                                        "150\t\t1.50%\n" +
                                        "200\t\t1.50%\n" +
                                        "250\t\t1.00%\n" +
                                        "500\t\t1.19%\n"),
                                getHP("0\t\t96.94%\n" +
                                        "900.0\t\t3.06%\n")
                        ),
                        new WeaponData(getMapFromString("2=6.0"),
                                0, 4000, 700,
                                getHP("0\t\t12.00%\n" +
                                        "30\t\t45.00%\n" +
                                        "50\t\t25.00%\n" +
                                        "60\t\t10.00%\n" +
                                        "70\t\t5.00%\n" +
                                        "80\t\t2.00%\n" +
                                        "100\t\t1.00%\n"),
                                getHP("0\t\t75.08%\n" +
                                        "80\t\t8.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "120\t\t3.00%\n" +
                                        "150\t\t3.00%\n" +
                                        "200\t\t2.00%\n" +
                                        "250\t\t2.00%\n" +
                                        "500\t\t1.92%\n"),
                                getHP("0\t\t97.46%\n" +
                                        "1500.0\t\t2.54%\n")
                        ),
                });
        freeWeaponData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        0, 500, 200,
                        getHP("0\t\t3.00%\n" +
                                "40\t\t46.00%\n" +
                                "60\t\t32.00%\n" +
                                "80\t\t8.00%\n" +
                                "100\t\t5.00%\n" +
                                "120\t\t3.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.00%\n"),
                        getHP("0\t\t25.32%\n" +
                                "60\t\t40.00%\n" +
                                "80\t\t23.00%\n" +
                                "100\t\t5.00%\n" +
                                "120\t\t3.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.20%\n" +
                                "250\t\t0.48%\n"),
                        getHP("0\t\t93.60%\n" +
                                "900.0\t\t6.40%\n"))});
        freeWeaponData.put(SpecialWeaponType.Lightning.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        0, 500, 200,
                        getHP("0\t\t1.00%\n" +
                                "40\t\t35.00%\n" +
                                "60\t\t30.00%\n" +
                                "80\t\t20.00%\n" +
                                "120\t\t7.00%\n" +
                                "150\t\t4.00%\n" +
                                "200\t\t2.00%\n" +
                                "220\t\t1.00%\n"),
                        getHP("0\t\t47.23%\n" +
                                "100\t\t20.00%\n" +
                                "120\t\t20.00%\n" +
                                "150\t\t8.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t1.00%\n" +
                                "300\t\t1.00%\n" +
                                "400\t\t0.78%\n"),
                        getHP("0\t\t93.14%\n" +
                                "1000.0\t\t6.86%\n"))});
        freeWeaponData.put(SpecialWeaponType.Napalm.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        0, 500, 200,
                        getHP("0\t\t5.00%\n" +
                                "40\t\t30.00%\n" +
                                "60\t\t30.00%\n" +
                                "80\t\t15.00%\n" +
                                "100\t\t12.00%\n" +
                                "150\t\t5.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t1.00%\n"),
                        getHP("0\t\t35.29%\n" +
                                "60\t\t20.00%\n" +
                                "80\t\t20.00%\n" +
                                "100\t\t9.00%\n" +
                                "150\t\t8.00%\n" +
                                "200\t\t4.00%\n" +
                                "250\t\t2.00%\n" +
                                "350\t\t1.71%\n"),
                        getHP("0\t\t94.33%\n" +
                                "1200.0\t\t5.67%\n"))});
        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        0, 500, 200,
                        getHP("0\t\t2.40%\n" +
                                "60\t\t70.00%\n" +
                                "80\t\t20.00%\n" +
                                "100\t\t4.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.00%\n" +
                                "250\t\t0.50%\n" +
                                "300\t\t0.10%\n"),
                        getHP("0\t\t50.99%\n" +
                                "100\t\t15.00%\n" +
                                "120\t\t20.00%\n" +
                                "150\t\t7.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t2.00%\n" +
                                "300\t\t1.00%\n" +
                                "500\t\t1.01%\n"),
                        getHP("0\t\t95.43%\n" +
                                "1500.0\t\t4.57%\n"))});
        freeWeaponData.put(SpecialWeaponType.Nuke.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        0, 500, 200,
                        getHP("0\t\t7.50%\n" +
                                "60\t\t37.00%\n" +
                                "80\t\t35.00%\n" +
                                "90\t\t12.00%\n" +
                                "100\t\t5.00%\n" +
                                "150\t\t2.00%\n" +
                                "250\t\t1.00%\n" +
                                "500\t\t0.50%\n"),
                        getHP("0\t\t38.32%\n" +
                                "80\t\t18.00%\n" +
                                "100\t\t18.00%\n" +
                                "120\t\t10.00%\n" +
                                "150\t\t10.00%\n" +
                                "200\t\t3.00%\n" +
                                "300\t\t2.50%\n" +
                                "600\t\t0.18%\n"),
                        getHP("0\t\t95.07%\n" +
                                "1500.0\t\t4.93%\n"))});

    }
}
