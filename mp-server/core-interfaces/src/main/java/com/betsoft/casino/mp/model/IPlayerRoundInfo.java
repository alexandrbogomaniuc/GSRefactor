package com.betsoft.casino.mp.model;

import com.abs.casino.common.cache.JsonSelfSerializable;
import com.abs.casino.common.util.Pair;
import com.esotericsoftware.kryo.KryoSerializable;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: flsh
 * Date: 12.02.19.
 */
public interface IPlayerRoundInfo<ENEMY_TYPE extends IEnemyType, ENEMY_STAT extends IEnemyStat, PRI extends IPlayerRoundInfo> extends KryoSerializable, JsonSelfSerializable<PRI> {
    ENEMY_TYPE[] getEnemyTypes();

    ENEMY_TYPE getEnemyTypeById(int id);

    List<ENEMY_TYPE> getBaseEnemies();

    Money getTotalBets();

    void addTotalBets(Money totalBets);

    Money getTotalPayouts();

    Money getRoundStartBalance();

    void addTotalPayouts(Money totalPayouts);

    IExperience getXpearned();

    void addXpearned(IExperience xpearned);

    long getPlayerRoundId();

    void setPlayerRoundId(long playerRoundId);

    long getRoomRoundId();

    void setRoomRoundId(long roomRoundId);

    long getRoomId();

    void setRoomId(long roomId);

    long getTimeOfRoundEnd();

    void setTimeOfRoundEnd(long timeOfRoundEnd);

    int getAmmoAmountBuyIn();

    void addAmmoAmountBuyIn(int ammoAmountByeIn);

    int getAmmoAmountReturned();

    void addAmmoAmountReturned(int ammoAmountReturned);

    void setAmmoAmountReturned(int ammoAmountReturned);

    long getRoomStake();

    void setRoomStake(long roomStake);

    Map<String, ENEMY_STAT> getStatByEnemies();

    void setStatByEnemies(Map<String, ENEMY_STAT> statByEnemies);

    void setTotalBets(Money totalBets);

    void setTotalPayouts(Money totalPayouts);

    void setRoundStartBalance(Money roundStartBalance);

    void setXpearned(IExperience xpearned);

    void setAmmoAmountBuyIn(int ammoAmountBuyIn);

    void updateStatOnEndRound(int ammoAmountBuyIn, IExperience xpearned, int ammoAmountReturned);

    String getWeaponSurplusVBA();

    void setWeaponSurplusVBA(String weaponSurplusVBA);

    void addWeaponSurplusVBA(String weaponSurplusVBA);

    Money getWeaponSurplusMoney();

    void setWeaponSurplusMoney(Money weaponSurplusMoney);

    void addWeaponSurplusMoney(Money weaponSurplusMoney);

    void updateStat(Money stake, boolean isBoss, Money extraBossPayout, Money mainBossPayout,
                    boolean isSpecialWeapon, String specialWeapon,
                    Money payout, boolean isKilled,
                    String enemy, Money betPayWeapon);

    void updateStatNew(Money stake, boolean isBoss, boolean isSpecialWeapon, String specialWeapon,
                    Money payout, boolean isKilled,
                    String enemyKey, Money betPayWeapon);

    void updateStatNewWithMultiplier (Money stake, boolean isBoss, boolean isSpecialWeapon, String specialWeapon,
                       Money payout, boolean isKilled,
                       String enemyKey, Money betPayWeapon, int chMult, String specialItemName);

    void updateQuestCompletedTotalData(Money win, int weaponTypeId, int newShots);

    IPlayerBet getPlayerBet(IPlayerBet newPlayerBet, int returnedBet);

    void updateAdditionalData(String currentModel);

    void setMaxShotTotalWin(long value);

    long getMaxShotTotalWin();

    Set<String> getWeaponTitles(int gameId);

    Map<String, Pair<AtomicInteger, AtomicDouble>> getAdditionalWins();

    String getRTPStatData(int gameId);

    void checkPay(Money checkPay);

    void addWeaponSourceStat(String source, String weaponTitle, int shots);

    void addMathHitCounter(int weaponId, int cnt);

    int getFreeShotsWon();

    int getMoneyWheelCompleted();

    long getMoneyWheelPayouts();

    void addDamage(double damage);

    double getTotalDamage();

    long getBattleBet();

    void setBattleBet(long battleBet);

    long getBattleWin();

    void setBattleWin(long battleWin);
}
