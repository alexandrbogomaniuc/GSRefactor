package com.betsoft.casino.mp.model;

import com.dgphoenix.casino.common.util.Pair;

import java.util.List;

/**
 * User: flsh
 * Date: 15.02.19.
 */
public interface IShootResult {
    List<Integer> getGems();

    void setGems(List<Integer> gems);

    Money getTotalGemsPayout();

    void setTotalGemsPayout(Money totalGemsPayout);

    Money getKillAwardWin();

    void setKillAwardWin(Money killAwardWin);

    int getMultiplierPay();

    void setMultiplierPay(int multiplierPay);

    int getNeedExplodeHP();

    void setNeedExplodeHP(int needExplodeHP);

    String getMineId();

    void setMineId(String mineId);

    IWeapon getWeapon();

    void setWeapon(IWeapon weapon);

    Money getWin();

    boolean isBossShouldBeAppeared();

    boolean isDestroyed();

    long getEnemyId();

    Money getBet();

    boolean isNewWeapon();

    void setNewWeapon(boolean newWeapon);

    Money getExtraBossBet();

    void setExtraBossBet(Money extraBossBet);

    Money getExtraBossWin();

    void setExtraBossWin(Money extraBossWin);

    void setBet(Money bet);

    void setWin(Money win);

    void setBossShouldBeAppeared(boolean bossShouldBeAppeared);

    void setDestroyed(boolean destroyed);

    IEnemy getEnemy();

    void setEnemy(IEnemy enemy);

    boolean isKilledMiss();

    double getDamage();

    void setDamage(double damage);

    boolean isNeedGenerateHVEnemy();

    void setNeedGenerateHVEnemy(boolean needGenerateHVEnemy);

    long getHvEnemyId();

    void setHvEnemyId(long hvEnemyId);

    boolean isShotToBoss();

    List<IWeaponSurplus> getWeaponSurpluses();

    void setWeaponSurpluses(List<IWeaponSurplus> weaponSurpluses);

    String getPrize();

    void setPrize(String prize);

    Pair<Integer, Integer> getNewFreeShots();

    void setNewFreeShots(Pair<Integer, Integer> newFreeShots);

    int getNewFreeShotsCount();

    EnemyAnimation getEnemyAnimation();

    void setEnemyAnimation(EnemyAnimation enemyAnimation);

    List<Pair<Integer, Money>> getAdditionalWins();

    void setAdditionalWins(List<Pair<Integer, Money>> additionalWins);

    int getBossSkinId();

    void setBossSkinId(int bossSkinId);

    boolean isInstanceKill();

    void setInstanceKill(boolean instanceKill);

    int getChMult();

    void setChMult(int chMult);

    List<ITransportWeapon> getAwardedWeapons();

    void setAwardedWeapons(List<ITransportWeapon> awardedWeapons);

    boolean isExplode();

    void setExplode(boolean explode);

    boolean isNeedExplode();

    void setNeedExplode(boolean needExplode);

    List<IEnemyResultPrize> getKilledEnemiesAndWins();

    void setKilledEnemiesAndWins(List<IEnemyResultPrize> killedEnemiesAndWins);

    boolean isInvulnerable();

    void setInvulnerable(boolean invulnerable);

    boolean isBossWin();

    Money getMoneyWheelWin();

    void setMoneyWheelWin(Money moneyWheelWin);

    boolean isMainShot();

    List<ISpinResult> getSpinResults();

    void setSpinResults(List<ISpinResult> spinResults);

    boolean isRage();

    void setRage(boolean rage);

    List<IDamage> getRageTargets();

    void setRageTargets(List<IDamage> rageTargets);

    List<IEnemyMode> getEnemiesWithUpdatedMode();

    void setEnemiesWithUpdatedMode(List<IEnemyMode> enemiesWithUpdatedMode);

    void addEffect(String effect);

    List<String> getEffects();
}
