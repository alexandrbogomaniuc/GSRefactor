package com.betsoft.casino.mp.piratescommon.model.math.data;

import com.betsoft.casino.mp.piratescommon.model.math.WeaponData;

import java.util.Collections;

import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getHP;
import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getMapFromString;

/**
 * Math table names: Enemy_4-8_*
 */
public class CrabData implements IEnemyData {
    @Override
    public int[] getLevels() {
        return new int[]{30, 40, 50};
    }

    @Override
    public WeaponData[] getTurretData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 3.0), 18,
                        getHP("0\t\t5,00%\n" +
                                "20\t\t80,00%\n" +
                                "30\t\t15,00%\n")),
                new WeaponData(Collections.singletonMap(2, 3.5), 15,
                        getHP("0\t\t0,00%\n" +
                                "20\t\t70,00%\n" +
                                "30\t\t25,00%\n" +
                                "40\t\t5,00%\n")),
                new WeaponData(Collections.singletonMap(2, 4.0), 12,
                        getHP("0\t\t5,00%\n" +
                                "20\t\t70,00%\n" +
                                "35\t\t20,00%\n" +
                                "45\t\t5,00%\n"))
        };
    }

    @Override
    public WeaponData[] getLaserData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t1,00%\n" +
                        "3\t\t0,50%\n" +
                        "5\t\t0,50%\n" +
                        "7\t\t0,25%\n" +
                        "10\t\t0,01%\n"), 30,
                        getHP("0\t\t7,00%\n" +
                                "30\t\t70,00%\n" +
                                "40\t\t20,00%\n" +
                                "50\t\t2,00%\n" +
                                "60\t\t1,00%\n"))
        };
    }

    @Override
    public WeaponData[] getLightningData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t5,00%\n" +
                        "3\t\t3,00%\n" +
                        "5\t\t2,00%\n"), 50,
                        getHP("0\t\t0,00%\n" +
                                "30\t\t65,00%\n" +
                                "40\t\t25,00%\n" +
                                "50\t\t6,00%\n" +
                                "70\t\t3,00%\n" +
                                "80\t\t1,00%\n"))
        };
    }

    @Override
    public WeaponData[] getNapalmData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t2,50%\n" +
                        "3\t\t1,00%\n" +
                        "5\t\t0,75%\n" +
                        "7\t\t0,50%\n" +
                        "10\t\t0,25%\n"), 20,
                        getHP("0\t\t2,00%\n" +
                                "30\t\t55,00%\n" +
                                "40\t\t25,00%\n" +
                                "50\t\t10,00%\n" +
                                "70\t\t5,00%\n" +
                                "80\t\t2,00%\n" +
                                "100\t\t1,00%\n"))
        };
    }

    @Override
    public WeaponData[] getArtilleryData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t2,50%\n" +
                        "3\t\t1,50%\n" +
                        "5\t\t1,00%\n" +
                        "7\t\t0,50%\n" +
                        "10\t\t0,25%\n"), 50,
                        getHP("0\t\t4,00%\n" +
                                "40\t\t75,00%\n" +
                                "50\t\t15,00%\n" +
                                "70\t\t3,00%\n" +
                                "100\t\t1,50%\n" +
                                "120\t\t1,00%\n" +
                                "150\t\t0,50%\n"))
        };
    }

    @Override
    public WeaponData[] getNukeData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t3,50%\n" +
                        "3\t\t2,00%\n" +
                        "5\t\t1,00%\n"), 40,
                        getHP("0\t\t1,50%\n" +
                                "40\t\t50,00%\n" +
                                "50\t\t35,00%\n" +
                                "60\t\t10,00%\n" +
                                "80\t\t2,00%\n" +
                                "100\t\t0,75%\n" +
                                "150\t\t0,50%\n" +
                                "200\t\t0,25%\n"))
        };
    }
}
