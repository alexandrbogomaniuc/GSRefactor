package com.betsoft.casino.mp.piratescommon.model.math.data;

import com.betsoft.casino.mp.piratescommon.model.math.WeaponData;

import java.util.Collections;

import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getHP;
import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getMapFromString;

/**
 * Math table names: Enemy_13-14_*
 */
public class NeckbeardData implements IEnemyData {
    @Override
    public int[] getLevels() {
        return new int[]{280, 300, 320};
    }

    @Override
    public WeaponData[] getTurretData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 6.0), 40, 35,
                        getHP("0\t\t36,00%\n" +
                                "15\t\t20,00%\n" +
                                "50\t\t30,00%\n" +
                                "70\t\t12,00%\n" +
                                "100\t\t2,00%\n"),
                        getHP("0\t\t76,33%\n" +
                                "120,0\t\t23,67%\n")),
                new WeaponData(Collections.singletonMap(2, 6.0), 35, 40,
                        getHP("0\t\t22,00%\n" +
                                "20\t\t25,00%\n" +
                                "50\t\t20,00%\n" +
                                "70\t\t20,00%\n" +
                                "80\t\t10,00%\n" +
                                "100\t\t2,00%\n" +
                                "120\t\t1,00%\n"),
                        getHP("0\t\t69,08%\n" +
                                "130,0\t\t30,92%\n")),
                new WeaponData(Collections.singletonMap(2, 6.0), 35, 50,
                        getHP("0\t\t22,00%\n" +
                                "20\t\t25,00%\n" +
                                "60\t\t30,00%\n" +
                                "80\t\t15,00%\n" +
                                "100\t\t5,00%\n" +
                                "120\t\t2,00%\n" +
                                "150\t\t1,00%\n"),
                        getHP("0\t\t68,64%\n" +
                                "140,0\t\t31,36%\n"))
        };
    }

    @Override
    public WeaponData[] getLaserData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t1,00%\n" +
                        "3\t\t0,50%\n" +
                        "5\t\t0,50%\n" +
                        "7\t\t0,25%\n" +
                        "10\t\t0,01%\n"), 45, 30,
                        getHP("0\t\t1,00%\n" +
                                "30\t\t73,00%\n" +
                                "40\t\t15,00%\n" +
                                "50\t\t5,00%\n" +
                                "80\t\t3,00%\n" +
                                "100\t\t1,00%\n" +
                                "120\t\t1,00%\n" +
                                "150\t\t1,00%\n"),
                        getHP("0\t\t77,19%\n" +
                                "160,0\t\t22,81%\n"))
        };
    }

    @Override
    public WeaponData[] getLightningData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t5,00%\n" +
                        "3\t\t3,00%\n" +
                        "5\t\t2,00%\n"), 50, 30,
                        getHP("0\t\t2,00%\n" +
                                "40\t\t72,00%\n" +
                                "50\t\t15,00%\n" +
                                "60\t\t5,00%\n" +
                                "80\t\t3,00%\n" +
                                "100\t\t1,00%\n" +
                                "150\t\t1,00%\n" +
                                "200\t\t1,00%\n"),
                        getHP("0\t\t72,82%\n" +
                                "170,0\t\t27,18%\n"))
        };
    }

    @Override
    public WeaponData[] getNapalmData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t2,50%\n" +
                        "3\t\t1,00%\n" +
                        "5\t\t0,75%\n" +
                        "7\t\t0,50%\n" +
                        "10\t\t0,25%\n"), 35, 30,
                        getHP("0\t\t2,00%\n" +
                                "40\t\t55,00%\n" +
                                "50\t\t21,00%\n" +
                                "60\t\t10,00%\n" +
                                "80\t\t6,00%\n" +
                                "100\t\t3,00%\n" +
                                "200\t\t2,00%\n" +
                                "250\t\t1,00%\n"),
                        getHP("0\t\t68,94%\n" +
                                "170,0\t\t31,06%\n"))
        };
    }

    @Override
    public WeaponData[] getArtilleryData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t2,50%\n" +
                        "3\t\t1,50%\n" +
                        "5\t\t1,00%\n" +
                        "7\t\t0,50%\n" +
                        "10\t\t0,25%\n"), 50, 70,
                        getHP("0\t\t2,00%\n" +
                                "70\t\t50,00%\n" +
                                "100\t\t30,00%\n" +
                                "150\t\t12,00%\n" +
                                "170\t\t3,00%\n" +
                                "200\t\t1,00%\n" +
                                "250\t\t1,00%\n" +
                                "350\t\t1,00%\n"),
                        getHP("0\t\t51,95%\n" +
                                "200,0\t\t48,05%\n"))
        };
    }

    @Override
    public WeaponData[] getNukeData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t3,50%\n" +
                        "3\t\t2,00%\n" +
                        "5\t\t1,00%\n"), 50, 40,
                        getHP("0\t\t2,00%\n" +
                                "80\t\t35,00%\n" +
                                "120\t\t40,00%\n" +
                                "150\t\t12,00%\n" +
                                "200\t\t5,00%\n" +
                                "250\t\t3,00%\n" +
                                "300\t\t2,00%\n" +
                                "500\t\t1,00%\n"),
                        getHP("0\t\t27,94%\n" +
                                "170,0\t\t72,06%\n"))
        };
    }
}
