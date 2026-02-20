package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IMiss;
import com.betsoft.casino.utils.TObject;

import java.util.List;

/**
 * User: flsh
 * Date: 11.06.17.
 */
public class Miss extends TObject implements IMiss {
    private int seatId;
    private boolean killedMiss;
    //if special weapon awarded, awardWeaponId contains weapon id, -1 if no award
    private int awardedWeaponId;
    private long enemyId;
    private int usedSpecialWeapon = -1;
    private int awardedWeaponShots;
    private int remainingSWShots;
    private double score;
    private boolean hit;
    private boolean lastResult;
    private float x;
    private float y;
    private String mineId;
    private long shotEnemyId;
    private boolean invulnerable;
    private int betLevel;
    private int serverAmmo;
    private String bulletId;
    private Integer fragmentId;
    private List<String> effects;
    private int bossNumberShots;

    public Miss(long date, int rid, int seatId, boolean killedMiss, int awardedWeaponId, long enemyId,
                int usedSpecialWeapon, int remainingSWShots, double score, boolean lastResult, float x, float y,
                int awardedWeaponShots, String mineId, long shotEnemyId, boolean invulnerable) {
        super(date, rid);
        this.seatId = seatId;
        this.killedMiss = killedMiss;
        this.awardedWeaponId = awardedWeaponId;
        this.enemyId = enemyId;
        this.usedSpecialWeapon = usedSpecialWeapon;
        this.remainingSWShots = remainingSWShots;
        this.score = score;
        this.lastResult = lastResult;
        this.x = x;
        this.y = y;
        this.awardedWeaponShots = awardedWeaponShots;
        this.mineId = mineId == null ? "" : mineId;
        this.shotEnemyId = shotEnemyId;
        this.invulnerable = invulnerable;
        this.betLevel = 1;
        this.bulletId = "";
    }

    public String getBulletId() {
        return bulletId;
    }

    public void setBulletId(String bulletId) {
        this.bulletId = bulletId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public boolean isKilledMiss() {
        return killedMiss;
    }

    public void setKilledMiss(boolean killedMiss) {
        this.killedMiss = killedMiss;
    }

    public int getUsedSpecialWeapon() {
        return usedSpecialWeapon;
    }

    public void setUsedSpecialWeapon(int usedSpecialWeapon) {
        this.usedSpecialWeapon = usedSpecialWeapon;
    }

    public int getRemainingSWShots() {
        return remainingSWShots;
    }

    public void setRemainingSWShots(int remainingSWShots) {
        this.remainingSWShots = remainingSWShots;
    }

    public int getAwardedWeaponId() {
        return awardedWeaponId;
    }

    public void setAwardedWeaponId(int awardedWeaponId) {
        this.awardedWeaponId = awardedWeaponId;
    }

    public long getEnemyId() {
        return enemyId;
    }

    public void setEnemyId(long enemyId) {
        this.enemyId = enemyId;
    }

    public boolean isHit() {
        return hit;
    }

    public double getScore() {
        return score;
    }

    public boolean isLastResult() {
        return lastResult;
    }

    public void setLastResult(boolean lastResult) {
        this.lastResult = lastResult;
    }

    public int getAwardedWeaponShots() {
        return awardedWeaponShots;
    }

    public void setAwardedWeaponShots(int awardedWeaponShots) {
        this.awardedWeaponShots = awardedWeaponShots;
    }

    public String getMineId() {
        return mineId;
    }

    public void setMineId(String mineId) {
        this.mineId = mineId;
    }

    public long getShotEnemyId() {
        return shotEnemyId;
    }

    public void setShotEnemyId(long shotEnemyId) {
        this.shotEnemyId = shotEnemyId;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public int getBetLevel() {
        return betLevel;
    }

    public void setBetLevel(int betLevel) {
        this.betLevel = betLevel;
    }

    @Override
    public int getServerAmmo() {
        return serverAmmo;
    }

    @Override
    public void setServerAmmo(int serverAmmo) {
        this.serverAmmo = serverAmmo;
    }

    public Integer getFragmentId() {
        return fragmentId;
    }

    public void setFragmentId(Integer fragmentId) {
        this.fragmentId = fragmentId;
    }

    @Override
    public List<String> getEffects() {
        return effects;
    }

    @Override
    public void setEffects(List<String> effects) {
        this.effects = effects;
    }

    public int getBossNumberShots() {
        return bossNumberShots;
    }

    public void setBossNumberShots(int bossNumberShots) {
        this.bossNumberShots = bossNumberShots;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Miss [");
        sb.append("rid=").append(rid);
        sb.append(", date=").append(date);
        sb.append(", seatId=").append(seatId);
        sb.append(", killedMiss=").append(killedMiss);
        sb.append(", awardedWeaponId=").append(awardedWeaponId);
        sb.append(", enemyId=").append(enemyId);
        sb.append(", usedSpecialWeapon=").append(usedSpecialWeapon);
        sb.append(", awardedWeaponShots=").append(awardedWeaponShots);
        sb.append(", remainingSWShots=").append(remainingSWShots);
        sb.append(", score=").append(score);
        sb.append(", hit=").append(hit);
        sb.append(", lastResult=").append(lastResult);
        sb.append(", x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", mineId=").append(mineId);
        sb.append(", shotEnemyId=").append(shotEnemyId);
        sb.append(", invulnerable=").append(invulnerable);
        sb.append(", betLevel=").append(betLevel);
        sb.append(", serverAmmo=").append(serverAmmo);
        sb.append(", bulletId=").append(bulletId);
        sb.append(", fragmentId=").append(fragmentId);
        sb.append(", effects=").append(effects);
        sb.append(", bossNumberShots=").append(bossNumberShots);
        sb.append(']');
        return sb.toString();
    }
}
