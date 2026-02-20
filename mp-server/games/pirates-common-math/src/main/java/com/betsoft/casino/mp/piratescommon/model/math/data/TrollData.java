package com.betsoft.casino.mp.piratescommon.model.math.data;

import com.betsoft.casino.mp.piratescommon.model.math.WeaponData;

import java.util.Collections;

import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getHP;
import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getMapFromString;

/**
 * Math table names: Enemy_19-20_*
 */
public class TrollData implements IEnemyData {
    @Override
    public int[] getLevels() {
        return new int[]{1200, 1350, 1500};
    }

    @Override
    public WeaponData[] getTurretData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 9.0), 50, 100,
                        getHP("0\t\t21,00%\n" +
                                "20\t\t25,00%\n" +
                                "80\t\t30,00%\n" +
                                "100\t\t15,00%\n" +
                                "120\t\t5,00%\n" +
                                "150\t\t2,00%\n" +
                                "200\t\t1,00%\n" +
                                "250\t\t1,00%\n"),
                        getHP("0\t\t80,83%\n" +
                                "300,0\t\t19,17%\n")),
                new WeaponData(Collections.singletonMap(2, 9.0), 50, 120,
                        getHP("0\t\t19,00%\n" +
                                "20\t\t15,00%\n" +
                                "80\t\t25,00%\n" +
                                "100\t\t25,00%\n" +
                                "150\t\t12,00%\n" +
                                "170\t\t2,00%\n" +
                                "200\t\t1,00%\n" +
                                "250\t\t1,00%\n"),
                        getHP("0\t\t78,89%\n" +
                                "350,0\t\t21,11%\n")),
                new WeaponData(Collections.singletonMap(2, 9.0), 50,140,
                        getHP("0\t\t9,00%\n" +
                                "20\t\t15,00%\n" +
                                "80\t\t30,00%\n" +
                                "100\t\t27,00%\n" +
                                "150\t\t10,00%\n" +
                                "170\t\t6,00%\n" +
                                "200\t\t2,00%\n" +
                                "250\t\t1,00%\n"),
                        getHP("0\t\t78,58%\n" +
                                "400,0\t\t21,43%\n"))
        };
    }

    @Override
    public WeaponData[] getLaserData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t1,00%\n" +
                        "3\t\t0,50%\n" +
                        "5\t\t0,50%\n" +
                        "7\t\t0,25%\n" +
                        "10\t\t0,01%\n"), 50, 30,
                        getHP("0\t\t3,00%\n" +
                                "40\t\t40,00%\n" +
                                "60\t\t30,00%\n" +
                                "80\t\t10,00%\n" +
                                "100\t\t8,00%\n" +
                                "120\t\t4,00%\n" +
                                "150\t\t3,00%\n" +
                                "200\t\t2,00%\n"),
                        getHP("0\t\t76,56%\n" +
                                "270,0\t\t23,44%\n"))
        };
    }

    @Override
    public WeaponData[] getLightningData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t5,00%\n" +
                        "3\t\t3,00%\n" +
                        "5\t\t2,00%\n"), 50, 40,
                        getHP("0\t\t4,00%\n" +
                                "40\t\t35,00%\n" +
                                "60\t\t25,00%\n" +
                                "80\t\t15,00%\n" +
                                "100\t\t12,00%\n" +
                                "150\t\t5,00%\n" +
                                "200\t\t2,00%\n" +
                                "220\t\t2,00%\n"),
                        getHP("0\t\t74,48%\n" +
                                "270,0\t\t25,52%\n"))
        };
    }

    @Override
    public WeaponData[] getNapalmData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t2,50%\n" +
                        "3\t\t1,00%\n" +
                        "5\t\t0,75%\n" +
                        "7\t\t0,50%\n" +
                        "10\t\t0,25%\n"), 45, 30,
                        getHP("0\t\t5,00%\n" +
                                "40\t\t30,00%\n" +
                                "60\t\t30,00%\n" +
                                "80\t\t15,00%\n" +
                                "100\t\t12,00%\n" +
                                "150\t\t5,00%\n" +
                                "200\t\t2,00%\n" +
                                "250\t\t1,00%\n"),
                        getHP("0\t\t77,33%\n" +
                                "300,0\t\t22,67%\n"))
        };
    }

    @Override
    public WeaponData[] getArtilleryData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t2,50%\n" +
                        "3\t\t1,50%\n" +
                        "5\t\t1,00%\n" +
                        "7\t\t0,50%\n" +
                        "10\t\t0,25%\n"), 50, 120,
                        getHP("0\t\t1,25%\n" +
                                "300\t\t60,00%\n" +
                                "400\t\t30,00%\n" +
                                "500\t\t5,00%\n" +
                                "750\t\t2,00%\n" +
                                "1500\t\t1,00%\n" +
                                "2500\t\t0,50%\n" +
                                "5000\t\t0,25%\n"),
                        getHP("0\t\t49,33%\n" +
                                "750,0\t\t50,67%\n"))
        };
    }

    @Override
    public WeaponData[] getNukeData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t3,50%\n" +
                        "3\t\t2,00%\n" +
                        "5\t\t1,00%\n"), 50, 100,
                        getHP("0\t\t8,00%\n" +
                                "150\t\t32,00%\n" +
                                "250\t\t35,00%\n" +
                                "500\t\t20,00%\n" +
                                "750\t\t2,00%\n" +
                                "1000\t\t1,00%\n" +
                                "1500\t\t1,00%\n" +
                                "2500\t\t1,00%\n"),
                        getHP("0\t\t39,90%\n" +
                                "500,0\t\t60,10%\n"))
        };
    }
}
