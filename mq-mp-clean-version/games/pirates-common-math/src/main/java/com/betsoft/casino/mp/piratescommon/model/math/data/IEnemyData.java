package com.betsoft.casino.mp.piratescommon.model.math.data;

import com.betsoft.casino.mp.piratescommon.model.math.WeaponData;

public interface IEnemyData {

    int[] getLevels();

    WeaponData[] getTurretData();
    WeaponData[] getLaserData();
    WeaponData[] getLightningData();
    WeaponData[] getNapalmData();
    WeaponData[] getArtilleryData();
    WeaponData[] getNukeData();
}
