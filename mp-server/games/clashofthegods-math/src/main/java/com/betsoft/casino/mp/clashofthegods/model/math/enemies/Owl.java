package com.betsoft.casino.mp.clashofthegods.model.math.enemies;
import com.betsoft.casino.mp.clashofthegods.model.math.WeaponData;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.abs.casino.common.util.Pair;
import java.util.Collections;
import static com.betsoft.casino.mp.clashofthegods.model.math.MathData.*;

public class Owl extends AbstractEnemyData{
    public Owl() {
        levels  = new int[]{250, 300, 350};

        weaponDropData.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(0.00, 0.0)); // pistol
        weaponDropData.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(40.00, 767.8)); // default data for enemy
        additionalWeaponKilledTable.put(PISTOL_DEFAULT_WEAPON_ID, Collections.EMPTY_LIST);
        additionalWeaponKilledTable.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t2\n" +
                "Lightning\t1\t\t\t2\n" +
                "Napalm\t1\t\t\t2\n" +
                "Artillery\t1\t\t\t1\n" +
                "Nuke\t1\t\t\t1\n"));

        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 4.0), 20.0, 30,
                                getHP("0\t\t20.00%\n" +
                                        "15\t\t40.00%\n" +
                                        "20\t\t20.00%\n" +
                                        "30\t\t15.00%\n" +
                                        "50\t\t5.00%\n"),
                                getHP("0\t\t57.50%\n" +
                                        "40.0\t\t42.50%\n")),
                        new WeaponData(Collections.singletonMap(2, 4.0), 18.0, 40,
                                getHP("0\t\t15.00%\n" +
                                        "20\t\t40.00%\n" +
                                        "30\t\t25.00%\n" +
                                        "40\t\t15.00%\n" +
                                        "50\t\t5.00%\n"),
                                getHP("0\t\t52.00%\n" +
                                        "50.0\t\t48.00%\n")),
                        new WeaponData(Collections.singletonMap(2, 4.0), 15.0, 40,
                                getHP("0\t\t5.00%\n" +
                                        "20\t\t40.00%\n" +
                                        "30\t\t30.00%\n" +
                                        "45\t\t20.00%\n" +
                                        "55\t\t5.00%\n"),
                                getHP("0\t\t52.08%\n" +
                                        "60.0\t\t47.92%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        40, 25,
                        getHP("0\t\t3.00%\n" +
                                "30\t\t55.00%\n" +
                                "40\t\t32.00%\n" +
                                "50\t\t5.00%\n" +
                                "70\t\t2.00%\n" +
                                "80\t\t1.00%\n" +
                                "100\t\t1.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t51.33%\n" +
                                "75.0\t\t48.67%\n"))});
        freeWeaponData.put(SpecialWeaponType.Lightning.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 25,
                        getHP("0\t\t1.00%\n" +
                                "30\t\t20.00%\n" +
                                "40\t\t35.00%\n" +
                                "50\t\t27.00%\n" +
                                "70\t\t10.00%\n" +
                                "80\t\t4.00%\n" +
                                "100\t\t2.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t52.80%\n" +
                                "100.0\t\t47.20%\n"))});

        freeWeaponData.put(SpecialWeaponType.Napalm.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        35, 25,
                        getHP("0\t\t2.00%\n" +
                                "30\t\t53.00%\n" +
                                "40\t\t20.00%\n" +
                                "50\t\t15.00%\n" +
                                "70\t\t4.00%\n" +
                                "80\t\t2.00%\n" +
                                "100\t\t2.00%\n" +
                                "150\t\t2.00%\n"),
                        getHP("0\t\t59.20%\n" +
                                "100.0\t\t40.80%\n"))});

        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 50,
                        getHP("0\t\t2.00%\n" +
                                "60\t\t50.00%\n" +
                                "80\t\t30.00%\n" +
                                "100\t\t12.00%\n" +
                                "120\t\t3.00%\n" +
                                "150\t\t1.50%\n" +
                                "200\t\t1.00%\n" +
                                "250\t\t0.50%\n"),
                        getHP("0\t\t49.93%\n" +
                                "150.0\t\t50.07%\n"))});
        freeWeaponData.put(SpecialWeaponType.Nuke.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        40, 35,
                        getHP("0\t\t3.25%\n" +
                                "80\t\t65.00%\n" +
                                "100\t\t20.00%\n" +
                                "120\t\t8.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.00%\n" +
                                "250\t\t0.50%\n" +
                                "300\t\t0.25%\n"),
                        getHP("0\t\t40.93%\n" +
                                "150.0\t\t59.07%\n"))});
    }
}
