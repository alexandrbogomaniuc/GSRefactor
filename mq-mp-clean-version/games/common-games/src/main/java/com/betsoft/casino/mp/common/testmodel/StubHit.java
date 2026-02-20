package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.utils.TObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class StubHit extends TObject implements IHit<StubRoomEnemy, IWinPrize, ITransportWeapon, IEnemyMode, IDamage, ISpin> {
    private int seatId;
    private double damage;
    private double win;
    //if special weapon awarded, awardWeaponId contains weapon id, -1 if no award
    private int awardedWeaponId = -1;
    private int usedSpecialWeapon = -1;
    private int remainingSWShots;
    private double score;
    private StubRoomEnemy enemy;
    private boolean hit;
    private int awardedWeaponShots;
    private boolean killed;
    private boolean lastResult;
    private int multiplierPay;
    private double killBonusPay;
    // TODO: remove after debug
    private double currentWin;
    private long hvEnemyId;
    private float x;
    private float y;
    private String mineId;
    private int newFreeShots;
    private int newFreeShotsSeatId;
    private Map<Integer, List<IWinPrize>> hitResultBySeats;
    private boolean instanceKill;
    private int chMult;
    private List<StubWeapon> awardedWeapons;
    private boolean needExplode;
    private boolean isExplode;
    private List<Integer> gems;
    private long enemyId;
    private long shotEnemyId;

    private int betLevel;
    private boolean isPaidSpecialShot;
    private double moneyWheelWin;

    private Map<Long, List<IWinPrize>> enemiesInstantKilled;
    private List<IDamage> rage;
    private List<String> effects;

    private Integer fragmentId;
    private Double gemsPayout;

    public StubHit(long date, int rid, int seatId, double damage, double win, int awardedWeaponId,
                   int usedSpecialWeapon, int remainingSWShots, double score, IRoomEnemy enemy, boolean lastResult,
                   double currentWin, long hvEnemyId, float x, float y, int awardedWeaponShots, boolean killed,
                   String mineId, int newFreeShots, int newFreeShotsSeatId, boolean instanceKill, int chMult,
                   long enemyId, long shotEnemyId) {
        super(date, rid);
        this.seatId = seatId;
        this.damage = damage;
        this.win = win;
        this.awardedWeaponId = awardedWeaponId;
        this.usedSpecialWeapon = usedSpecialWeapon;
        this.remainingSWShots = remainingSWShots;
        this.score = score;
        this.enemy = StubRoomEnemy.convert(enemy);
        this.lastResult = lastResult;
        this.currentWin = currentWin;
        this.hvEnemyId = hvEnemyId;
        this.x = x;
        this.y = y;
        this.hit = true;
        this.awardedWeaponShots = awardedWeaponShots;
        this.killed = killed;
        this.mineId = mineId == null ? "" : mineId;
        this.newFreeShots = newFreeShots;
        this.newFreeShotsSeatId = newFreeShotsSeatId;
        this.hitResultBySeats = new HashMap<>();
        this.instanceKill = instanceKill;
        this.chMult = chMult;
        this.isExplode = false;
        this.needExplode = false;
        this.killBonusPay = 0;
        this.multiplierPay = 0;
        this.gems = new LinkedList<>();
        this.enemyId = enemyId;
        this.shotEnemyId = shotEnemyId;
        this.enemiesInstantKilled = new HashMap<>();
        this.moneyWheelWin = 0;
    }

    @Override
    public List<Integer> getGems() {
        return gems;
    }

    @Override
    public void setGems(List<Integer> gems) {
        this.gems = gems;
    }

    @Override
    public int getSeatId() {
        return seatId;
    }

    @Override
    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    @Override
    public double getDamage() {
        return damage;
    }

    @Override
    public void setDamage(int damage) {
        this.damage = damage;
    }

    @Override
    public double getWin() {
        return win;
    }

    @Override
    public void setWin(double win) {
        this.win = win;
    }

    @Override
    public int getAwardedWeaponId() {
        return awardedWeaponId;
    }

    @Override
    public void setAwardedWeaponId(int awardedWeaponId) {
        this.awardedWeaponId = awardedWeaponId;
    }

    @Override
    public StubRoomEnemy getEnemy() {
        return enemy;
    }

    @Override
    public void setEnemy(StubRoomEnemy enemy) {
        this.enemy = enemy;
    }

    @Override
    public int getUsedSpecialWeapon() {
        return usedSpecialWeapon;
    }

    @Override
    public void setUsedSpecialWeapon(int usedSpecialWeapon) {
        this.usedSpecialWeapon = usedSpecialWeapon;
    }

    @Override
    public int getRemainingSWShots() {
        return remainingSWShots;
    }

    @Override
    public void setRemainingSWShots(int remainingSWShots) {
        this.remainingSWShots = remainingSWShots;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public boolean isLastResult() {
        return lastResult;
    }

    @Override
    public void setLastResult(boolean lastResult) {
        this.lastResult = lastResult;
    }

    @Override
    public int getServerAmmo() {
        return 0;
    }

    @Override
    public void setServerAmmo(int serverAmmo) {

    }

    @Override
    public long getHvEnemyId() {
        return hvEnemyId;
    }

    @Override
    public void setHvEnemyId(long hvEnemyId) {
        this.hvEnemyId = hvEnemyId;
    }

    @Override
    public boolean isHit() {
        return hit;
    }

    @Override
    public void setHit(boolean hit) {
        this.hit = hit;
    }

    @Override
    public int getAwardedWeaponShots() {
        return awardedWeaponShots;
    }

    @Override
    public void setAwardedWeaponShots(int awardedWeaponShots) {
        this.awardedWeaponShots = awardedWeaponShots;
    }

    @Override
    public String getMineId() {
        return mineId;
    }

    @Override
    public void setMineId(String mineId) {
        this.mineId = mineId;
    }

    @Override
    public int getNewFreeShots() {
        return newFreeShots;
    }

    @Override
    public void setNewFreeShots(int newFreeShots) {
        this.newFreeShots = newFreeShots;
    }

    @Override
    public int getNewFreeShotsSeatId() {
        return newFreeShotsSeatId;
    }

    @Override
    public void setNewFreeShotsSeatId(int newFreeShotsSeatId) {
        this.newFreeShotsSeatId = newFreeShotsSeatId;
    }

    @Override
    public boolean isInstanceKill() {
        return instanceKill;
    }

    @Override
    public void setInstanceKill(boolean instanceKill) {
        this.instanceKill = instanceKill;
    }

    @Override
    public int getChMult() {
        return chMult;
    }

    @Override
    public void setChMult(int chMult) {
        this.chMult = chMult;
    }

    @Override
    public boolean isNeedExplode() {
        return needExplode;
    }

    @Override
    public void setNeedExplode(boolean needExplode) {
        this.needExplode = needExplode;
    }

    @Override
    public boolean isExplode() {
        return isExplode;
    }

    @Override
    public void setExplode(boolean explode) {
        isExplode = explode;
    }

    @Override
    public List<ITransportWeapon> getAwardedWeapons() {
        return null;
    }

    @Override
    public void setAwardedWeapons(List<ITransportWeapon> awardedWeapons) {

    }

    @Override
    public int getMultiplierPay() {
        return multiplierPay;
    }

    @Override
    public void setMultiplierPay(int multiplierPay) {
        this.multiplierPay = multiplierPay;
    }

    @Override
    public double getKillBonusPay() {
        return killBonusPay;
    }

    @Override
    public void setKillBonusPay(double killBonusPay) {
        this.killBonusPay = killBonusPay;
    }

    @Override
    public long getEnemyId() {
        return enemyId;
    }

    @Override
    public void setEnemyId(long enemyId) {
        this.enemyId = enemyId;
    }

    @Override
    public long getShotEnemyId() {
        return shotEnemyId;
    }

    @Override
    public void setShotEnemyId(long shotEnemyId) {
        this.shotEnemyId = shotEnemyId;
    }

    @Override
    public int getBetLevel() {
        return betLevel;
    }

    @Override
    public void setBetLevel(int betLevel) {
        this.betLevel = betLevel;
    }

    @Override
    public boolean isPaidSpecialShot() {
        return isPaidSpecialShot;
    }

    @Override
    public void setPaidSpecialShot(boolean paidSpecialShot) {
        isPaidSpecialShot = paidSpecialShot;
    }

    @Override
    public Map<Long, List<IWinPrize>> getEnemiesInstantKilled() {
        return enemiesInstantKilled;
    }

    @Override
    public void setEnemiesInstantKilled(Map<Long, List<IWinPrize>> enemiesInstantKilled) {
        this.enemiesInstantKilled = enemiesInstantKilled;
    }

    @Override
    public double getMoneyWheelWin() {
        return 0;
    }

    @Override
    public void setMoneyWheelWin(double moneyWheelWin) {

    }

    @Override
    public List<IDamage> getRage() {
        return rage;
    }

    @Override
    public void setRage(List<IDamage> rage) {
        this.rage = rage;
    }

    @Override
    public List<String> getEffects() {
        return effects;
    }

    @Override
    public void setEffects(List<String> effects) {
        this.effects = effects;
    }

    @Override
    public int getBossNumberShots() {
        return 0;
    }

    @Override
    public void setBossNumberShots(int bossNumberShots) {

    }

    @Override
    public void addEffect(String effect) {

    }

    @Override
    public List<IEnemyMode> getEnemiesWithUpdatedMode() {
        return null;
    }

    @Override
    public void setEnemiesWithUpdatedMode(List<IEnemyMode> enemiesWithUpdatedMode) {

    }

    @Override
    public String getBulletId() {
        return null;
    }

    @Override
    public void setBulletId(String bulletId) {

    }

    @Override
    public Integer getFragmentId() {
        return fragmentId;
    }

    @Override
    public void setFragmentId(Integer fragmentId) {
        this.fragmentId = fragmentId;
    }

    @Override
    public List<ISpin> getSlot() {
        return null;
    }

    @Override
    public void setSlot(List<ISpin> spins) {

    }

    @Override
    public int getNextBetLevel() {
        return 0;
    }

    @Override
    public void setNextBetLevel(int nextBetLevel) {

    }

    @Override
    public Double getGemsPayout() {
        return null;
    }

    @Override
    public void setGemsPayout(Double gemsPayout) {
    }

    @Override
    public void setCurrentPowerUpMultiplier(int currentPowerUpMultiplier) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        StubHit hit = (StubHit) o;

        if (damage != hit.damage) return false;
        return enemy.equals(hit.enemy);
    }

    public Map<Integer, List<IWinPrize>> getHitResultBySeats() {
        return hitResultBySeats == null ? new HashMap<>() : hitResultBySeats;
    }

    public void setHitResultBySeats(Map<Integer, List<IWinPrize>> hitResultBySeats) {
        this.hitResultBySeats = hitResultBySeats;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Hit [");
        sb.append("rid=").append(rid);
        sb.append(", date=").append(date);
        sb.append(", seatId=").append(seatId);
        sb.append(", damage=").append(damage);
        sb.append(", win=").append(win);
        sb.append(", awardedWeaponId=").append(awardedWeaponId);
        sb.append(", usedSpecialWeapon=").append(usedSpecialWeapon);
        sb.append(", remainingSWShots=").append(remainingSWShots);
        sb.append(", score=").append(score);
        sb.append(", enemy=").append(enemy);
        sb.append(", hit=").append(hit);
        sb.append(", awardedWeaponShots=").append(awardedWeaponShots);
        sb.append(", lastResult=").append(lastResult);
        sb.append(", currentWin=").append(currentWin);
        sb.append(", hvEnemyId=").append(hvEnemyId);
        sb.append(", x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", mineId=").append(mineId);
        sb.append(", newFreeShots=").append(newFreeShots);
        sb.append(", newFreeShotsSeatId=").append(newFreeShotsSeatId);
        sb.append(", hitResultBySeats=").append(hitResultBySeats);
        sb.append(", instanceKill=").append(instanceKill);
        sb.append(", chMult=").append(chMult);
        sb.append(", awardedWeapons=").append(awardedWeapons);
        sb.append(", needExplode=").append(needExplode);
        sb.append(", isExplode=").append(isExplode);
        sb.append(", killBonusPay=").append(killBonusPay);
        sb.append(", multiplierPay=").append(multiplierPay);
        sb.append(", gems=").append(gems);
        sb.append(", enemyId=").append(enemyId);
        sb.append(", shotEnemyId=").append(shotEnemyId);
        sb.append(", enemiesInstantKilled=").append(enemiesInstantKilled);
        sb.append(", betLevel=").append(betLevel);
        sb.append(", isPaidSpecialShot=").append(isPaidSpecialShot);
        sb.append(", moneyWheelWin=").append(moneyWheelWin);
        sb.append(", rage=").append(rage);
        sb.append(", effects=").append(effects);
        sb.append(", fragmentId=").append(fragmentId);
        sb.append(']');
        return sb.toString();
    }
}
