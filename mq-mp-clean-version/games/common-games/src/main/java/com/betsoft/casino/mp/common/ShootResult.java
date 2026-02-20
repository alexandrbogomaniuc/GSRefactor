package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.dgphoenix.casino.common.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * User: flsh
 * Date: 14.02.19.
 */
public class ShootResult implements IShootResult {
    /** bet of shot, for free SW is zero  */
    private Money bet;
    /** main win of shot */
    private Money win;
    /** amount of damage for old action games */
    private double damage;
    private boolean bossShouldBeAppeared;
    /** true if enemy was destroyed */
    private boolean destroyed;
    private IWeapon weapon;
    private boolean isNewWeapon;
    private Money extraBossBet;
    private Money extraBossWin;
    private IEnemy enemy;
    private boolean needGenerateHVEnemy;
    private long hvEnemyId;
    private boolean isShotToBoss;
    /** list of possible compensation for poor shot (for complex old games)   */
    private List<IWeaponSurplus> weaponSurpluses;
    private String mineId;
    private String prize;
    private Pair<Integer, Integer> newFreeShots;
    private EnemyAnimation enemyAnimation;
    /** additional wins for old games  */
    private List<Pair<Integer, Money>> additionalWins;
    private int bossSkinId;
    private boolean instanceKill;
    /** additional multiplier for old games */
    private int chMult;
    /** list awarded weapons  */
    private List<ITransportWeapon> awardedWeapons;
    private int needExplodeHP;
    private boolean isExplode;
    private boolean needExplode;
    /** additional win for killing   */
    private Money killAwardWin;
    /** additional multiplier for old games */
    private int multiplierPay;
    private List<Integer> gems;
    private Money totalGemsPayout;
    private List<IEnemyResultPrize> killedEnemiesAndWins;
    /** enemy was invulnerable, need return shot  */
    private boolean invulnerable;
    private Money additionalCompensateWin;
    private Money moneyWheelWin;
    /** last shot or not, for pistol always true, for SW can be false for not last shots  */
    private boolean isMainShot;
    private List<ISpinResult> spinResults;
    private boolean rage;
    private List<IDamage> rageTargets;
    private List<IEnemyMode> enemiesWithUpdatedMode;
    private List<String> effects;

    public ShootResult(Money bet, Money win, boolean bossShouldBeAppeared, boolean destroyed, IEnemy enemy) {
        this.bet = bet;
        this.win = win;
        this.bossShouldBeAppeared = bossShouldBeAppeared;
        this.destroyed = destroyed;
        this.enemy = enemy;
        this.hvEnemyId = -1; // no HV enemy id.
        if (enemy != null) {
            this.isShotToBoss = enemy.isBoss();
        }
        mineId = "";
        prize = "";
        enemyAnimation = EnemyAnimation.NO_ANIMATION;
        additionalWins = new ArrayList<>();
        bossSkinId = -1;
        awardedWeapons = new ArrayList<>();
        needExplodeHP = 0;
        isExplode = false;
        needExplode = false;
        multiplierPay = 0;
        killAwardWin = Money.ZERO;
        totalGemsPayout = Money.ZERO;
        gems = new LinkedList<>();
        killedEnemiesAndWins = new ArrayList<>();
        invulnerable = false;
        additionalCompensateWin = Money.ZERO;
        moneyWheelWin = Money.ZERO;
        isMainShot = false;
        rage = false;
    }

    public boolean isMainShot() {
        return isMainShot;
    }

    public void setMainShot(boolean mainShot) {
        isMainShot = mainShot;
    }

    public ShootResult(Money bet, Money win, boolean bossShouldBeAppeared, boolean destroyed, IEnemy enemy,
                       boolean invulnerable) {
        this(bet, win, bossShouldBeAppeared, destroyed, enemy);
        this.invulnerable = invulnerable;
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
    public Money getTotalGemsPayout() {
        return totalGemsPayout;
    }

    @Override
    public void setTotalGemsPayout(Money totalGemsPayout) {
        this.totalGemsPayout = totalGemsPayout;
    }

    @Override
    public Money getKillAwardWin() {
        return killAwardWin;
    }

    @Override
    public void setKillAwardWin(Money killAwardWin) {
        this.killAwardWin = killAwardWin;
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
    public int getNeedExplodeHP() {
        return needExplodeHP;
    }

    @Override
    public void setNeedExplodeHP(int needExplodeHP) {
        this.needExplodeHP = needExplodeHP;
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
    public IWeapon getWeapon() {
        return weapon;
    }

    @Override
    public void setWeapon(IWeapon weapon) {
        this.weapon = weapon;
    }

    @Override
    public Money getWin() {
        return win;
    }

    @Override
    public boolean isBossShouldBeAppeared() {
        return bossShouldBeAppeared;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public long getEnemyId() {
        return enemy.getId();
    }

    @Override
    public Money getBet() {
        return bet;
    }

    @Override
    public boolean isNewWeapon() {
        return isNewWeapon;
    }

    @Override
    public void setNewWeapon(boolean newWeapon) {
        isNewWeapon = newWeapon;
    }

    @Override
    public Money getExtraBossBet() {
        return extraBossBet;
    }

    @Override
    public void setExtraBossBet(Money extraBossBet) {
        this.extraBossBet = extraBossBet;
    }

    @Override
    public Money getExtraBossWin() {
        return extraBossWin;
    }

    @Override
    public void setExtraBossWin(Money extraBossWin) {
        this.extraBossWin = extraBossWin;
    }

    @Override
    public void setBet(Money bet) {
        this.bet = bet;
    }

    @Override
    public void setWin(Money win) {
        this.win = win;
    }

    @Override
    public void setBossShouldBeAppeared(boolean bossShouldBeAppeared) {
        this.bossShouldBeAppeared = bossShouldBeAppeared;
    }

    @Override
    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    @Override
    public IEnemy getEnemy() {
        return enemy;
    }

    @Override
    public void setEnemy(IEnemy enemy) {
        this.enemy = enemy;
    }

    @Override
    public boolean isKilledMiss() {
        return enemy == null;
    }

    @Override
    public double getDamage() {
        return damage;
    }

    @Override
    public void setDamage(double damage) {
        this.damage = damage;
    }

    @Override
    public boolean isNeedGenerateHVEnemy() {
        return needGenerateHVEnemy;
    }

    @Override
    public void setNeedGenerateHVEnemy(boolean needGenerateHVEnemy) {
        this.needGenerateHVEnemy = needGenerateHVEnemy;
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
    public boolean isShotToBoss() {
        return isShotToBoss;
    }

    @Override
    public List<IWeaponSurplus> getWeaponSurpluses() {
        return weaponSurpluses;
    }

    @Override
    public void setWeaponSurpluses(List<IWeaponSurplus> weaponSurpluses) {
        this.weaponSurpluses = weaponSurpluses;
    }

    @Override
    public String getPrize() {
        return prize;
    }

    @Override
    public void setPrize(String prize) {
        this.prize = prize;
    }

    @Override
    public Pair<Integer, Integer> getNewFreeShots() {
        return newFreeShots;
    }

    @Override
    public void setNewFreeShots(Pair<Integer, Integer> newFreeShots) {
        this.newFreeShots = newFreeShots;
    }

    @Override
    public int getNewFreeShotsCount() {
        return newFreeShots == null ? 0 : newFreeShots.getValue();
    }

    @Override
    public EnemyAnimation getEnemyAnimation() {
        return enemyAnimation;
    }

    @Override
    public void setEnemyAnimation(EnemyAnimation enemyAnimation) {
        this.enemyAnimation = enemyAnimation;
    }

    @Override
    public List<Pair<Integer, Money>> getAdditionalWins() {
        return additionalWins;
    }

    @Override
    public void setAdditionalWins(List<Pair<Integer, Money>> additionalWins) {
        this.additionalWins = additionalWins;
    }

    @Override
    public int getBossSkinId() {
        return bossSkinId;
    }

    @Override
    public void setBossSkinId(int bossSkinId) {
        this.bossSkinId = bossSkinId;
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
    public List<ITransportWeapon> getAwardedWeapons() {
        return awardedWeapons;
    }

    @Override
    public void setAwardedWeapons(List<ITransportWeapon> awardedWeapons) {
        this.awardedWeapons = awardedWeapons;
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
    public boolean isNeedExplode() {
        return needExplode;
    }

    @Override
    public void setNeedExplode(boolean needExplode) {
        this.needExplode = needExplode;
    }

    @Override
    public List<IEnemyResultPrize> getKilledEnemiesAndWins() {
        return killedEnemiesAndWins;
    }

    @Override
    public void setKilledEnemiesAndWins(List<IEnemyResultPrize> killedEnemiesAndWins) {
        this.killedEnemiesAndWins = killedEnemiesAndWins;
    }

    @Override
    public boolean isInvulnerable() {
        return invulnerable;
    }

    @Override
    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public boolean isBossWin() {
        return isShotToBoss() && (getWin().greaterThan(Money.ZERO) || (additionalWins != null && !additionalWins.isEmpty()));
    }

    public Money getAdditionalCompensateWin() {
        return additionalCompensateWin;
    }

    public void setAdditionalCompensateWin(Money additionalCompensateWin) {
        this.additionalCompensateWin = additionalCompensateWin;
    }

    @Override
    public Money getMoneyWheelWin() {
        return moneyWheelWin;
    }

    @Override
    public void setMoneyWheelWin(Money moneyWheelWin) {
        this.moneyWheelWin = moneyWheelWin;
    }

    @Override
    public List<ISpinResult> getSpinResults() {
        return spinResults;
    }

    public void setSpinResults(List<ISpinResult> spinResults) {
        this.spinResults = spinResults;
    }

    public boolean isRage() {
        return rage;
    }

    public void setRage(boolean rage) {
        this.rage = rage;
    }

    public List<IDamage> getRageTargets() {
        return rageTargets;
    }

    public void setRageTargets(List<IDamage> rageTargets) {
        this.rageTargets = rageTargets;
    }

    public List<IEnemyMode> getEnemiesWithUpdatedMode() {
        return enemiesWithUpdatedMode;
    }

    public void setEnemiesWithUpdatedMode(List<IEnemyMode> enemiesWithUpdatedMode) {
        this.enemiesWithUpdatedMode = enemiesWithUpdatedMode;
    }

    @Override
    public List<String> getEffects() {
        return effects;
    }

    @Override
    public void addEffect(String effect) {
        if (effects == null) {
            effects = new ArrayList<>();
        }
        effects.add(effect);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ShootResult[");
        sb.append("bet=").append(bet);
        sb.append(", win=").append(win);
        sb.append(", bossShouldBeAppeared=").append(bossShouldBeAppeared);
        sb.append(", destroyed=").append(destroyed);
        sb.append(", enemy=").append(enemy != null ? enemy.getEnemyClass().getEnemyType().getName() : null);
        sb.append(", weapon=").append(weapon);
        sb.append(", isNewWeapon=").append(isNewWeapon);
        sb.append(", extraBossBet=").append(extraBossBet);
        sb.append(", extraBossWin=").append(extraBossWin);
        sb.append(", damage=").append(damage);
        sb.append(", needGenerateHVEnemy=").append(needGenerateHVEnemy);
        sb.append(", hvEnemyId=").append(hvEnemyId);
        sb.append(", weaponSurpluses=").append(weaponSurpluses);
        sb.append(", mineId=").append(mineId);
        sb.append(", prize=").append(prize);
        sb.append(", newFreeShots=").append(newFreeShots);
        sb.append(", enemyAnimation=").append(enemyAnimation);
        sb.append(", additionalWins=").append(additionalWins);
        sb.append(", bossSkinId=").append(bossSkinId);
        sb.append(", instanceKill=").append(instanceKill);
        sb.append(", chMult=").append(chMult);
        sb.append(", awardedWeapons=").append(awardedWeapons);
        sb.append(", needExplodeHP=").append(needExplodeHP);
        sb.append(", isExplode=").append(isExplode);
        sb.append(", needExplode=").append(needExplode);
        sb.append(", killAwardWin=").append(killAwardWin);
        sb.append(", multiplierPay=").append(multiplierPay);
        sb.append(", gems=").append(gems);
        sb.append(", totalGemsPayout=").append(totalGemsPayout);
        sb.append(", killedEnemiesAndWins=").append(killedEnemiesAndWins);
        sb.append(", invulnerable=").append(invulnerable);
        sb.append(", additionalCompensateWin=").append(additionalCompensateWin);
        sb.append(", moneyWheelWin=").append(moneyWheelWin);
        sb.append(", isMainShot=").append(isMainShot);
        sb.append(", spinResults=").append(spinResults);
        sb.append(", rage=").append(rage);
        sb.append(", rageTargets=").append(rageTargets);
        sb.append(", enemiesWithUpdatedMode=").append(enemiesWithUpdatedMode);
        sb.append(", effects=").append(effects);
        sb.append(']');
        return sb.toString();
    }

}
