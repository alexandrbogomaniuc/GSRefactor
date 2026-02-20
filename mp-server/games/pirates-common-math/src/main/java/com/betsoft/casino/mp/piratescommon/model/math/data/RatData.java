package com.betsoft.casino.mp.piratescommon.model.math.data;

import com.betsoft.casino.mp.piratescommon.model.math.WeaponData;

import java.util.Collections;

import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getHP;
import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getMapFromString;

/**
 * Math table names: Enemy_1-3_*
 */
public class RatData implements IEnemyData {

    @Override
    public int[] getLevels() {
        return new int[]{20, 25, 30};
    }

    @Override
    public WeaponData[] getTurretData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 4.0), 20,
                        getHP("0\t\t10,00%\n" +
                                "10\t\t65,00%\n" +
                                "20\t\t25,00%\n")),
                new WeaponData(Collections.singletonMap(2, 4.0), 15,
                        getHP("0\t\t10,00%\n" +
                                "15\t\t70,00%\n" +
                                "25\t\t20,00%\n")),
                new WeaponData(Collections.singletonMap(2, 4.0), 12,
                        getHP("0\t\t10,00%\n" +
                                "20\t\t80,00%\n" +
                                "30\t\t10,00%\n"))
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
                        getHP("0\t\t1,00%\n" +
                                "30\t\t80,00%\n" +
                                "40\t\t18,00%\n" +
                                "50\t\t1,00%\n"))
        };
    }

    @Override
    public WeaponData[] getLightningData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t5,00%\n" +
                        "3\t\t3,00%\n" +
                        "5\t\t2,00%\n"), 50,
                        getHP("0\t\t0,00%\n" +
                                "30\t\t70,00%\n" +
                                "40\t\t20,00%\n" +
                                "50\t\t6,00%\n" +
                                "60\t\t4,00%\n"))
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
                        getHP("0\t\t1,00%\n" +
                                "30\t\t55,00%\n" +
                                "40\t\t25,00%\n" +
                                "50\t\t10,00%\n" +
                                "70\t\t7,00%\n" +
                                "80\t\t2,00%\n"))
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
                        getHP("0\t\t1,00%\n" +
                                "40\t\t85,00%\n" +
                                "50\t\t10,00%\n" +
                                "70\t\t3,00%\n" +
                                "100\t\t1,00%\n"))
        };
    }

    @Override
    public WeaponData[] getNukeData() {
        return new WeaponData[]{
                new WeaponData(getMapFromString("2\t\t3,50%\n" +
                        "3\t\t2,00%\n" +
                        "5\t\t1,00%\n"), 40,
                        getHP("0\t\t0,75%\n" +
                                "40\t\t63,00%\n" +
                                "50\t\t25,00%\n" +
                                "60\t\t8,00%\n" +
                                "70\t\t2,00%\n" +
                                "100\t\t0,75%\n" +
                                "120\t\t0,50%\n"))
        };
    }
}
