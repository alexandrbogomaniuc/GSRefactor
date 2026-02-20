package com.betsoft.casino.mp.model;

import com.dgphoenix.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.KryoSerializable;

import java.util.Map;

/**
 * User: flsh
 * Date: 12.02.19.
 */
public interface IEnemyStat<WEAPON_STAT extends IWeaponStat, ES extends IEnemyStat> 
        extends KryoSerializable, JsonSelfSerializable<ES> {

    void updateData(Money stake,boolean isSpecial, String specialWeapon, Money payout, boolean isKilled, Money betPayWeapon);

    void updateData(Money stake,boolean isSpecial, String specialWeapon, Money payout, boolean isKilled, Money betPayWeapon,
                    int chMult, String specialItemName);
    void updateKillAwardWin(Money killAwardWin);
    void updatePayoutsFromItems(Money payout, String specialItemName);

    int getCntShotsToEnemy();

    Money getPayouts();

    int getCntKills();

    Money getHvBets();

    void setHvBets(Money hvBets);

    boolean isHighValueEnemy();

    Money getMainBets();

    void setMainBets(Money mainBets);

    Money getTurretBets();

    Map<String, WEAPON_STAT> getSpecialWeaponsStats();

    Map<Integer, Integer> getChMultipliers();

    void setCntShotsToEnemy(int cntShotsToEnemy);

    void setPayouts(Money payouts);

    void setCntKills(int cntKills);

    void setSpecialWeaponsStats(Map<String, WEAPON_STAT> specialWeaponsStats);

    void setHighValueEnemy(boolean highValueEnemy);

    void updateKillAwardWinWithLevelUp(Money killAwardWin, boolean isSpecialWeapon, String specialWeapon);
}
