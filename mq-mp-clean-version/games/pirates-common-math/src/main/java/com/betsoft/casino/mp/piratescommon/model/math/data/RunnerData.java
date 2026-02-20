package com.betsoft.casino.mp.piratescommon.model.math.data;

import com.betsoft.casino.mp.piratescommon.model.math.WeaponData;

import java.util.Collections;

import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getHP;
import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getMapFromString;

/**
 * Math table names: Enemy_17-18_*
 */
public class RunnerData implements IEnemyData {
    @Override
    public int[] getLevels() {
        return new int[]{700, 750, 800};
    }

    @Override
    public WeaponData[] getTurretData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 8.0), 50, 50,
                        getHP("0\t\t1,00%\n" +
                                "20\t\t25,00%\n" +
                                "30\t\t35,00%\n" +
                                "50\t\t30,00%\n" +
                                "100\t\t5,00%\n" +
                                "120\t\t2,00%\n" +
                                "150\t\t1,00%\n" +
                                "200\t\t1,00%\n"),
                        getHP("0\t\t79,30%\n" +
                                "200,0\t\t20,70%\n")),
                new WeaponData(Collections.singletonMap(2, 8.0), 45, 60,
                        getHP("0\t\t4,00%\n" +
                                "20\t\t30,00%\n" +
                                "30\t\t30,00%\n" +
                                "60\t\t20,00%\n" +
                                "100\t\t10,00%\n" +
                                "120\t\t3,00%\n" +
                                "150\t\t2,00%\n" +
                                "200\t\t1,00%\n"),
                        getHP("0\t\t78,29%\n" +
                                "210,0\t\t21,71%\n")),
                new WeaponData(Collections.singletonMap(2, 8.0), 40, 70,
                        getHP("0\t\t4,00%\n" +
                                "20\t\t35,00%\n" +
                                "40\t\t35,00%\n" +
                                "80\t\t10,00%\n" +
                                "100\t\t8,00%\n" +
                                "150\t\t5,00%\n" +
                                "200\t\t2,00%\n" +
                                "220\t\t1,00%\n"),
                        getHP("0\t\t76,95%\n" +
                                "220,0\t\t23,05%\n"))
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
                        getHP("0\t\t10,00%\n" +
                                "40\t\t40,00%\n" +
                                "60\t\t30,00%\n" +
                                "80\t\t10,00%\n" +
                                "100\t\t4,00%\n" +
                                "120\t\t3,00%\n" +
                                "150\t\t2,00%\n" +
                                "170\t\t1,00%\n"),
                        getHP("0\t\t78,28%\n" +
                                "250,0\t\t21,72%\n"))
        };
    }

    @Override
    public WeaponData[] getLightningData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t5,00%\n" +
                        "3\t\t3,00%\n" +
                        "5\t\t2,00%\n"), 50, 40,
                        getHP("0\t\t6,00%\n" +
                                "60\t\t25,00%\n" +
                                "70\t\t35,00%\n" +
                                "80\t\t20,00%\n" +
                                "100\t\t8,00%\n" +
                                "120\t\t3,00%\n" +
                                "150\t\t2,00%\n" +
                                "200\t\t1,00%\n"),
                        getHP("0\t\t72,27%\n" +
                                "260,0\t\t27,73%\n"))
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
                                "40\t\t35,00%\n" +
                                "60\t\t30,00%\n" +
                                "80\t\t15,00%\n" +
                                "100\t\t7,00%\n" +
                                "120\t\t5,00%\n" +
                                "150\t\t2,00%\n" +
                                "200\t\t1,00%\n"),
                        getHP("0\t\t75,20%\n" +
                                "250,0\t\t24,80%\n"))
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
                        getHP("0\t\t4,00%\n" +
                                "200\t\t20,00%\n" +
                                "300\t\t50,00%\n" +
                                "500\t\t15,00%\n" +
                                "750\t\t5,00%\n" +
                                "1000\t\t3,00%\n" +
                                "1200\t\t2,00%\n" +
                                "1500\t\t1,00%\n"),
                        getHP("0\t\t25,70%\n" +
                                "500,0\t\t74,30%\n"))
        };
    }

    @Override
    public WeaponData[] getNukeData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t3,50%\n" +
                        "3\t\t2,00%\n" +
                        "5\t\t1,00%\n"), 50, 100,
                        getHP("0\t\t1,00%\n" +
                                "150\t\t32,00%\n" +
                                "200\t\t35,00%\n" +
                                "250\t\t25,00%\n" +
                                "500\t\t3,00%\n" +
                                "750\t\t2,00%\n" +
                                "1000\t\t1,00%\n" +
                                "1500\t\t1,00%\n"),
                        getHP("0\t\t41,13%\n" +
                                "400,0\t\t58,88%\n"))
        };
    }
}
