package com.betsoft.casino.mp.piratescommon.model.math.data;

import com.betsoft.casino.mp.piratescommon.model.math.WeaponData;

import java.util.Collections;

import static com.betsoft.casino.mp.piratescommon.model.math.MathData.getHP;

/**
 * Math table name: Weapon Carrier
 */
public class WeaponCarrierData implements IEnemyData {
    @Override
    public int[] getLevels() {
        return new int[]{400, 450, 500};
    }

    @Override
    public WeaponData[] getTurretData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 4.0), 0, 70,
                        getHP("0\t\t74,00%\n" +
                                "40\t\t1,00%\n" +
                                "50\t\t5,00%\n" +
                                "60\t\t20,00%\n"),
                        getHP("0\t\t98,25%\n" +
                                "850,0\t\t1,75%\n"))
        };
    }

    @Override
    public WeaponData[] getLaserData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 9.0), 0, 100,
                        getHP("0\t\t1,00%\n" +
                                "20\t\t30,00%\n" +
                                "30\t\t33,00%\n" +
                                "40\t\t17,00%\n" +
                                "50\t\t12,00%\n" +
                                "70\t\t5,00%\n" +
                                "100\t\t2,00%\n"),
                        getHP("0\t\t96,07%\n" +
                                "870,0\t\t3,93%\n"))
        };
    }

    @Override
    public WeaponData[] getLightningData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 10.0), 0, 120,
                        getHP("0\t\t1,00%\n" +
                                "30\t\t45,00%\n" +
                                "40\t\t25,00%\n" +
                                "50\t\t15,00%\n" +
                                "60\t\t8,00%\n" +
                                "80\t\t3,00%\n" +
                                "100\t\t2,00%\n" +
                                "120\t\t1,00%\n"),
                        getHP("0\t\t95,40%\n" +
                                "900,0\t\t4,60%\n"))
        };
    }

    @Override
    public WeaponData[] getNapalmData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 10.0), 0, 120,
                        getHP("0\t\t4,00%\n" +
                                "30\t\t35,00%\n" +
                                "40\t\t30,00%\n" +
                                "50\t\t17,00%\n" +
                                "60\t\t10,00%\n" +
                                "80\t\t2,00%\n" +
                                "100\t\t1,00%\n" +
                                "120\t\t1,00%\n"),
                        getHP("0\t\t95,71%\n" +
                                "950,0\t\t4,29%\n"))
        };
    }

    @Override
    public WeaponData[] getArtilleryData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 10.0), 0, 300,
                        getHP("0\t\t0,00%\n" +
                                "50\t\t40,00%\n" +
                                "80\t\t30,00%\n" +
                                "100\t\t20,00%\n" +
                                "150\t\t6,00%\n" +
                                "200\t\t2,00%\n" +
                                "250\t\t1,00%\n" +
                                "300\t\t1,00%\n"),
                        getHP("0\t\t92,50%\n" +
                                "1100,0\t\t7,50%\n"))
        };
    }

    @Override
    public WeaponData[] getNukeData() {
        return new WeaponData[]{
                new WeaponData(Collections.singletonMap(2, 8.0), 0, 350,
                        getHP("0\t\t1,00%\n" +
                                "60\t\t45,00%\n" +
                                "80\t\t25,00%\n" +
                                "100\t\t15,00%\n" +
                                "150\t\t8,00%\n" +
                                "200\t\t3,00%\n" +
                                "300\t\t2,00%\n" +
                                "350\t\t1,00%\n"),
                        getHP("0\t\t92,22%\n" +
                                "1150,0\t\t7,78%\n"))
        };
    }
}
