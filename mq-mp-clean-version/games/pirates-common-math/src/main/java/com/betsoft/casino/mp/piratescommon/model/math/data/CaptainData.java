package com.betsoft.casino.mp.piratescommon.model.math.data;

import com.betsoft.casino.mp.piratescommon.model.math.WeaponData;

import java.util.Collections;

import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getHP;
import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getMapFromString;

/**
 * Math table names: Enemy_15-16_*
 */
public class CaptainData implements IEnemyData {
    @Override
    public int[] getLevels() {
        return new int[]{450, 475, 500};
    }

    @Override
    public WeaponData[] getTurretData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 6.0), 40, 70,
                        getHP("0\t\t4,00%\n" +
                                "30\t\t60,00%\n" +
                                "60\t\t30,00%\n" +
                                "100\t\t5,00%\n" +
                                "150\t\t1,00%\n"),
                        getHP("0\t\t78,75%\n" +
                                "200,0\t\t21,25%\n")),
                new WeaponData(Collections.singletonMap(2, 6.0), 35, 80,
                        getHP("0\t\t0,00%\n" +
                                "30\t\t72,00%\n" +
                                "70\t\t20,00%\n" +
                                "100\t\t5,00%\n" +
                                "120\t\t2,00%\n" +
                                "150\t\t1,00%\n"),
                        getHP("0\t\t78,81%\n" +
                                "210,0\t\t21,19%\n")),
                new WeaponData(Collections.singletonMap(2, 6.0), 35, 90,
                        getHP("0\t\t2,00%\n" +
                                "30\t\t75,00%\n" +
                                "70\t\t12,00%\n" +
                                "100\t\t8,00%\n" +
                                "150\t\t2,00%\n" +
                                "200\t\t1,00%\n"),
                        getHP("0\t\t80,05%\n" +
                                "220,0\t\t19,95%\n"))
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
                                "30\t\t46,00%\n" +
                                "40\t\t30,00%\n" +
                                "50\t\t10,00%\n" +
                                "70\t\t5,00%\n" +
                                "80\t\t3,00%\n" +
                                "100\t\t3,00%\n" +
                                "200\t\t2,00%\n"),
                        getHP("0\t\t80,14%\n" +
                                "220,0\t\t19,86%\n"))
        };
    }

    @Override
    public WeaponData[] getLightningData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t5,00%\n" +
                        "3\t\t3,00%\n" +
                        "5\t\t2,00%\n"), 50, 20,
                        getHP("0\t\t2,00%\n" +
                                "40\t\t27,00%\n" +
                                "50\t\t35,00%\n" +
                                "60\t\t23,00%\n" +
                                "80\t\t6,00%\n" +
                                "100\t\t4,00%\n" +
                                "200\t\t2,00%\n" +
                                "250\t\t1,00%\n"),
                        getHP("0\t\t73,91%\n" +
                                "220,0\t\t26,09%\n"))
        };
    }

    @Override
    public WeaponData[] getNapalmData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t2,50%\n" +
                        "3\t\t1,00%\n" +
                        "5\t\t0,75%\n" +
                        "7\t\t0,50%\n" +
                        "10\t\t0,25%\n"), 40, 30,
                        getHP("0\t\t2,00%\n" +
                                "40\t\t50,00%\n" +
                                "50\t\t20,00%\n" +
                                "60\t\t10,00%\n" +
                                "80\t\t8,00%\n" +
                                "100\t\t5,00%\n" +
                                "200\t\t3,00%\n" +
                                "300\t\t2,00%\n"),
                        getHP("0\t\t73,00%\n" +
                                "220,0\t\t27,00%\n"))
        };
    }

    @Override
    public WeaponData[] getArtilleryData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t2,50%\n" +
                        "3\t\t1,50%\n" +
                        "5\t\t1,00%\n" +
                        "7\t\t0,50%\n" +
                        "10\t\t0,25%\n"), 50, 80,
                        getHP("0\t\t3,50%\n" +
                                "100\t\t20,00%\n" +
                                "120\t\t50,00%\n" +
                                "150\t\t16,00%\n" +
                                "200\t\t7,00%\n" +
                                "250\t\t2,00%\n" +
                                "300\t\t1,00%\n" +
                                "500\t\t0,50%\n"),
                        getHP("0\t\t57,17%\n" +
                                "300,0\t\t42,83%\n"))
        };
    }

    @Override
    public WeaponData[] getNukeData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t3,50%\n" +
                        "3\t\t2,00%\n" +
                        "5\t\t1,00%\n"), 50, 50,
                        getHP("0\t\t0,00%\n" +
                                "100\t\t40,00%\n" +
                                "120\t\t40,00%\n" +
                                "150\t\t12,00%\n" +
                                "200\t\t3,00%\n" +
                                "250\t\t2,00%\n" +
                                "500\t\t2,00%\n" +
                                "750\t\t1,00%\n"),
                        getHP("0\t\t55,17%\n" +
                                "300,0\t\t44,83%\n"))
        };
    }
}
