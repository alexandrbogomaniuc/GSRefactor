package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.common.Coords;
import com.betsoft.casino.mp.common.MinePoint;
import com.betsoft.casino.mp.common.SeatBullet;
import com.abs.casino.common.exception.CommonException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 18.01.2022.
 */
public interface IActionGameSeat<WEAPON extends IWeapon, PLAYER_ROUND_INFO extends IActionGamePlayerRoundInfo<?, ?, ?>, TREASURE extends ITreasure, RPI extends IRoomPlayerInfo, S extends ISeat>
        extends ISingleNodeSeat<WEAPON, PLAYER_ROUND_INFO, TREASURE, RPI, S> {
    List<IMinePlace> getMinePlaces(Coords coords);
    IFreeShots getFreeShots();
    int getSpecialWeaponId();
    int getSpecialWeaponRemaining();
    void setWeapon(int weaponId);
    void resetWeapons();
    void consumeSpecialWeapon(int weaponId);
    WEAPON getCurrentWeapon();
    void setWeapons(Map<Integer, WEAPON> weapons);
    void addWeapon(int weaponId, int shots);
    void addWeapon(WEAPON weapon);
    int getCurrentWeaponId();
    Map<SpecialWeaponType, WEAPON> getWeapons();
    boolean isAnyWeaponShotAvailable();
    void setWeaponSurplus(ArrayList<IWeaponSurplus> weaponSurplus);
    List<IWeaponSurplus> getWeaponSurplus();
    Money getCompensateSpecialWeapons();
    void setCompensateSpecialWeapons(Money compensateSpecialWeapons);
    Money getTotalReturnedSpecialWeapons();
    void setTotalReturnedSpecialWeapons(Money totalReturnedSpecialWeapons);
    void addTreasures(List<TREASURE> treasures);
    Map<TREASURE, Integer> getRoundTreasures();
    default List<MinePoint> getSeatMines() {
        return new ArrayList<>();
    }
    void setSeatMines(List<MinePoint> seatMines);
    Map<Long, Double> getDamageToEnemies();
    default Double getDamageForEnemyId(long enemyId) {
        return 0.;
    }
    default void removeDamageForEnemyId(long enemyId) {
    }
    int getTotalTreasuresCount();
    void setTotalTreasuresCount(int totalTreasuresCount);

    List<IWeaponSurplus> getWeaponsReturned();

    void setWeaponsReturned(List<IWeaponSurplus> weaponsReturned);

    Map<String, Boolean> getMineStates();

    void setMineStates(Map<String, Boolean> mineStates);

    void addMineState(String mineId, boolean mineState);

    int getHitCount();

    void setHitCount(int hitCount);

    void incrementHitsCount();

    int getMissCount();

    void setMissCount(int missCount);

    void incrementMissCount();

    int getEnemiesKilledCount();

    void setEnemiesKilledCount(int enemiesKilledCount);

    void incCountEnemiesKilled();

    void setBulletsFired(int bulletsFired);

    void incrementBulletsFired();

    int getBulletsFired();

    Set<SeatBullet> getBulletsOnMap();

    boolean addSeatBullet(SeatBullet seatBullet);

    void removeBulletById(String bulletId);

    SeatBullet getBulletById(String bulletId);

    long getTotalBossPayout();

    void addTotalBossPayout(long bossPayout);

    void resetTotalBossPayout();

    double getTotalTreasuresXP();

    long getTotalTreasuresXPAsLong();

    void setTotalTreasuresXP(double totalTreasuresXP);

    void addTotalTreasuresXP(double totalTreasuresXP);

    double getTotalKillsXP();

    long getTotalKillsXPAsLong();

    void setTotalKillsXP(double totalKillsXP);

    void addTotalKillsXP(double totalKillsXP);

    int getAmmoAmount();

    int getAmmoAmountTotalInRound();

    void setAmmoAmount(int ammoAmount);

    void decrementAmmoAmount();

    void decrementAmmoAmount(int decrement);

    void incrementAmmoAmount(int ammoAmount);

    void incrementTotalAmmoAmount(int increment);

    void transferWinToAmmo() throws CommonException;

    Money retrieveRemainingAmmo();

}
