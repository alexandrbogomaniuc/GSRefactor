package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface IHit<ROOM_ENEMY extends IRoomEnemy, PRIZE extends IWinPrize, WEAPON extends ITransportWeapon,
        ENEMY_MODE extends IEnemyMode, DAMAGE extends IDamage, SPIN extends ISpin>
        extends ITransportObject, IServerMessage, IShotResult {
    List<Integer> getGems();

    void setGems(List<Integer> gems);

    int getSeatId();

    void setSeatId(int seatId);

    double getDamage();

    void setDamage(int damage);

    double getWin();

    void setWin(double win);

    int getAwardedWeaponId();

    void setAwardedWeaponId(int awardedWeaponId);

    ROOM_ENEMY getEnemy();

    void setEnemy(ROOM_ENEMY enemy);

    int getUsedSpecialWeapon();

    void setUsedSpecialWeapon(int usedSpecialWeapon);

    int getRemainingSWShots();

    void setRemainingSWShots(int remainingSWShots);

    double getScore();

    void setLastResult(boolean lastResult);

    long getHvEnemyId();

    void setHvEnemyId(long hvEnemyId);

    boolean isHit();

    void setHit(boolean hit);

    int getAwardedWeaponShots();

    void setAwardedWeaponShots(int awardedWeaponShots);

    String getMineId();

    void setMineId(String mineId);

    int getNewFreeShots();

    void setNewFreeShots(int newFreeShots);

    int getNewFreeShotsSeatId();

    void setNewFreeShotsSeatId(int newFreeShotsSeatId);

    boolean isInstanceKill();

    void setInstanceKill(boolean instanceKill);

    int getChMult();

    void setChMult(int chMult);

    boolean isNeedExplode();

    void setNeedExplode(boolean needExplode);

    boolean isExplode();

    void setExplode(boolean explode);

    List<WEAPON> getAwardedWeapons();

    void setAwardedWeapons(List<WEAPON> awardedWeapons);

    int getMultiplierPay();

    void setMultiplierPay(int multiplierPay);

    double getKillBonusPay();

    void setKillBonusPay(double killBonusPay);

    long getEnemyId();

    void setEnemyId(long enemyId);

    long getShotEnemyId();

    void setShotEnemyId(long shotEnemyId);

    Map<Long, List<PRIZE>> getEnemiesInstantKilled();

    void setEnemiesInstantKilled(Map<Long, List<PRIZE>> enemiesInstantKilled);

    Map<Integer, List<PRIZE>> getHitResultBySeats();

    void setHitResultBySeats(Map<Integer, List<PRIZE>> hitResultBySeats);

    int getBetLevel();

    void setBetLevel(int betLevel);

    boolean isPaidSpecialShot();

    void setPaidSpecialShot(boolean paidSpecialShot);

    double getMoneyWheelWin();

    void setMoneyWheelWin(double moneyWheelWin);

    List<DAMAGE> getRage();

    void setRage(List<DAMAGE> rage);

    List<String> getEffects();

    void setEffects(List<String> effects);

    void addEffect(String effect);

    List<ENEMY_MODE> getEnemiesWithUpdatedMode();

    void setEnemiesWithUpdatedMode(List<ENEMY_MODE> enemiesWithUpdatedMode);

    String getBulletId();

    void setBulletId(String bulletId);

    List<SPIN> getSlot();

    void setSlot(List<SPIN> spins);

    int getNextBetLevel();

    void setNextBetLevel(int nextBetLevel);

    Double getGemsPayout();

    void setGemsPayout(Double gemsPayout);

    void setCurrentPowerUpMultiplier(int currentPowerUpMultiplier);
}
