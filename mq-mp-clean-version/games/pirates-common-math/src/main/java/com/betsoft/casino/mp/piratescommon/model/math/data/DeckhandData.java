package com.betsoft.casino.mp.piratescommon.model.math.data;

import com.betsoft.casino.mp.piratescommon.model.math.WeaponData;

import java.util.Collections;

import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getHP;
import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getMapFromString;

/**
 * Math table names: Enemy_11-12_*
 */
public class DeckhandData implements IEnemyData {
    @Override
    public int[] getLevels() {
        return new int[]{180, 200, 220};
    }

    @Override
    public WeaponData[] getTurretData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 5.0), 35, 30,
                        getHP("0\t\t36,00%\n" +
                                "15\t\t30,00%\n" +
                                "40\t\t22,00%\n" +
                                "60\t\t10,00%\n" +
                                "100\t\t2,00%\n"),
                        getHP("0\t\t69,57%\n" +
                                "70,0\t\t30,43%\n")),
                new WeaponData(Collections.singletonMap(2, 5.0), 30, 35,
                        getHP("0\t\t15,00%\n" +
                                "20\t\t30,00%\n" +
                                "40\t\t30,00%\n" +
                                "60\t\t16,00%\n" +
                                "80\t\t8,00%\n" +
                                "100\t\t1,00%\n"),
                        getHP("0\t\t56,25%\n" +
                                "80,0\t\t43,75%\n")),
                new WeaponData(Collections.singletonMap(2, 5.0), 30, 40,
                        getHP("0\t\t38,00%\n" +
                                "20\t\t30,00%\n" +
                                "60\t\t20,00%\n" +
                                "80\t\t8,00%\n" +
                                "100\t\t3,00%\n" +
                                "120\t\t1,00%\n"),
                        getHP("0\t\t68,22%\n" +
                                "90,0\t\t31,78%\n"))
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
                        getHP("0\t\t1,25%\n" +
                                "30\t\t75,00%\n" +
                                "40\t\t15,00%\n" +
                                "50\t\t5,00%\n" +
                                "70\t\t2,00%\n" +
                                "80\t\t1,00%\n" +
                                "100\t\t0,50%\n" +
                                "120\t\t0,25%\n"),
                        getHP("0\t\t71,67%\n" +
                                "120,0\t\t28,33%\n"))
        };
    }

    @Override
    public WeaponData[] getLightningData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t5,00%\n" +
                        "3\t\t3,00%\n" +
                        "5\t\t2,00%\n"), 50, 30,
                        getHP("0\t\t1,25%\n" +
                                "40\t\t75,00%\n" +
                                "50\t\t15,00%\n" +
                                "60\t\t5,00%\n" +
                                "70\t\t2,00%\n" +
                                "80\t\t1,00%\n" +
                                "100\t\t0,50%\n" +
                                "150\t\t0,25%\n"),
                        getHP("0\t\t66,48%\n" +
                                "130,0\t\t33,52%\n"))
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
                        getHP("0\t\t4,50%\n" +
                                "30\t\t55,00%\n" +
                                "50\t\t21,00%\n" +
                                "60\t\t10,00%\n" +
                                "70\t\t6,00%\n" +
                                "80\t\t2,00%\n" +
                                "100\t\t1,00%\n" +
                                "150\t\t0,50%\n"),
                        getHP("0\t\t68,81%\n" +
                                "130,0\t\t31,19%\n"))
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
                        getHP("0\t\t0,25%\n" +
                                "50\t\t53,00%\n" +
                                "70\t\t30,00%\n" +
                                "100\t\t12,00%\n" +
                                "120\t\t3,00%\n" +
                                "150\t\t1,00%\n" +
                                "200\t\t0,50%\n" +
                                "250\t\t0,25%\n"),
                        getHP("0\t\t58,61%\n" +
                                "160,0\t\t41,39%\n"))
        };
    }

    @Override
    public WeaponData[] getNukeData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t3,50%\n" +
                        "3\t\t2,00%\n" +
                        "5\t\t1,00%\n"), 50, 40,
                        getHP("0\t\t3,50%\n" +
                                "70\t\t40,00%\n" +
                                "100\t\t40,00%\n" +
                                "120\t\t10,00%\n" +
                                "150\t\t3,00%\n" +
                                "200\t\t2,00%\n" +
                                "250\t\t1,00%\n" +
                                "350\t\t0,50%\n"),
                        getHP("0\t\t28,65%\n" +
                                "130,0\t\t71,35%\n"))
        };
    }
}
