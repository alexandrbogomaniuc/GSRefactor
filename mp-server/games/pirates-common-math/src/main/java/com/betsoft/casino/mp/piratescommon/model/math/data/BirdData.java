package com.betsoft.casino.mp.piratescommon.model.math.data;

import com.betsoft.casino.mp.piratescommon.model.math.WeaponData;

import java.util.Collections;

import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getHP;
import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getMapFromString;

/**
 * Math table names: Enemy_9-10_*
 */
public class BirdData implements IEnemyData {

    @Override
    public int[] getLevels() {
        return new int[]{80, 100, 120};
    }

    @Override
    public WeaponData[] getTurretData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 4.0), 20, 30,
                        getHP("0\t\t20,00%\n" +
                                "15\t\t40,00%\n" +
                                "20\t\t25,00%\n" +
                                "30\t\t15,00%\n"),
                        getHP("0\t\t61,25%\n" +
                                "40,0\t\t38,75%\n")),
                new WeaponData(Collections.singletonMap(2, 4.0), 18, 40,
                        getHP("0\t\t15,00%\n" +
                                "20\t\t40,00%\n" +
                                "30\t\t25,00%\n" +
                                "40\t\t20,00%\n"),
                        getHP("0\t\t53,00%\n" +
                                "50,0\t\t47,00%\n")),
                new WeaponData(Collections.singletonMap(2, 4.0), 15, 40,
                        getHP("0\t\t10,00%\n" +
                                "20\t\t40,00%\n" +
                                "30\t\t30,00%\n" +
                                "45\t\t20,00%\n"),
                        getHP("0\t\t56,67%\n" +
                                "60,0\t\t43,33%\n"))
        };
    }

    @Override
    public WeaponData[] getLaserData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t1,00%\n" +
                        "3\t\t0,50%\n" +
                        "5\t\t0,50%\n" +
                        "7\t\t0,25%\n" +
                        "10\t\t0,01%\n"), 40, 25,
                        getHP("0\t\t3,00%\n" +
                                "30\t\t55,00%\n" +
                                "40\t\t32,00%\n" +
                                "50\t\t5,00%\n" +
                                "70\t\t2,00%\n" +
                                "80\t\t1,00%\n" +
                                "100\t\t1,00%\n" +
                                "150\t\t1,00%\n"),
                        getHP("0\t\t51,33%\n" +
                                "75,0\t\t48,67%\n"))
        };
    }

    @Override
    public WeaponData[] getLightningData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t5,00%\n" +
                        "3\t\t3,00%\n" +
                        "5\t\t2,00%\n"), 50, 25,
                        getHP("0\t\t1,00%\n" +
                                "30\t\t20,00%\n" +
                                "40\t\t35,00%\n" +
                                "50\t\t27,00%\n" +
                                "70\t\t10,00%\n" +
                                "80\t\t4,00%\n" +
                                "100\t\t2,00%\n" +
                                "150\t\t1,00%\n"),
                        getHP("0\t\t52,80%\n" +
                                "100,0\t\t47,20%\n"))
        };
    }

    @Override
    public WeaponData[] getNapalmData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t2,50%\n" +
                        "3\t\t1,00%\n" +
                        "5\t\t0,75%\n" +
                        "7\t\t0,50%\n" +
                        "10\t\t0,25%\n"), 35, 25,
                        getHP("0\t\t2,00%\n" +
                                "30\t\t53,00%\n" +
                                "40\t\t20,00%\n" +
                                "50\t\t15,00%\n" +
                                "70\t\t4,00%\n" +
                                "80\t\t2,00%\n" +
                                "100\t\t2,00%\n" +
                                "150\t\t2,00%\n"),
                        getHP("0\t\t59,20%\n" +
                                "100,0\t\t40,80%\n"))
        };
    }

    @Override
    public WeaponData[] getArtilleryData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t2,50%\n" +
                        "3\t\t1,50%\n" +
                        "5\t\t1,00%\n" +
                        "7\t\t0,50%\n" +
                        "10\t\t0,25%\n"), 50, 50,
                        getHP("0\t\t2,00%\n" +
                                "60\t\t50,00%\n" +
                                "80\t\t30,00%\n" +
                                "100\t\t12,00%\n" +
                                "120\t\t3,00%\n" +
                                "150\t\t1,50%\n" +
                                "170\t\t1,00%\n" +
                                "200\t\t0,50%\n"),
                        getHP("0\t\t50,30%\n" +
                                "150,0\t\t49,70%\n"))
        };
    }

    @Override
    public WeaponData[] getNukeData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t3,50%\n" +
                        "3\t\t2,00%\n" +
                        "5\t\t1,00%\n"), 40, 35,
                        getHP("0\t\t3,25%\n" +
                                "80\t\t65,00%\n" +
                                "100\t\t20,00%\n" +
                                "120\t\t8,00%\n" +
                                "150\t\t2,00%\n" +
                                "200\t\t1,00%\n" +
                                "250\t\t0,50%\n" +
                                "300\t\t0,25%\n"),
                        getHP("0\t\t40,93%\n" +
                                "150,0\t\t59,07%\n"))
        };
    }
}
