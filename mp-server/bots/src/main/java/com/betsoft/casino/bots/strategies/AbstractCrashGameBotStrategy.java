package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.bots.AstronautBetData;
import com.betsoft.casino.bots.IUnifiedBotStrategy;
import com.abs.casino.common.util.RNG;
import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class AbstractCrashGameBotStrategy implements IUnifiedBotStrategy {

    int numberRoundsBeforeRestart;

    public AbstractCrashGameBotStrategy(int numberRoundsBeforeRestart) {
        this.numberRoundsBeforeRestart = numberRoundsBeforeRestart;
    }

    @Override
    public int getNumberRoundBeforeRestart() {
        return numberRoundsBeforeRestart;
    }

    public double getPlayerRandomMultForRange(int min, int max) {
        double temMult = RNG.nextInt(min, max) + RNG.rand();
        return BigDecimal.valueOf(temMult).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    abstract public AstronautBetData generateMultiplierForFirst(String nickname);

    @Override
    abstract public AstronautBetData generateMultiplierForSecond(String nickname);

    @Override
    abstract public AstronautBetData generateMultiplierForThird(String nickname);

    @Override
    public int getShots() {
        return 0;
    }

    @Override
    public void resetShots() {

    }

    @Override
    public boolean shouldShoot(String botId) {
        return false;
    }

    @Override
    public boolean shouldPurchaseWeaponLootBox() {
        return false;
    }

    @Override
    public boolean shouldSwitchWeapon() {
        return false;
    }

    @Override
    public boolean shouldPurchaseBullets() {
        return false;
    }

    @Override
    public long getWaitTime() {
        return 0;
    }

    @Override
    public int getBuyInAmmoAmount(long balance, float stake, int minAmmo) {
        return 0;
    }

    @Override
    public void addWeapon(int id, int shots) {

    }

    @Override
    public int getWeaponId() {
        return 0;
    }

    @Override
    public void consumeAmmo(int weaponId) {

    }

    @Override
    public void resetWeapons() {

    }

    @Override
    public int getShotsForWeapon(int weaponId) {
        return 0;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractCrashGameBotStrategy{");
        sb.append("numberRoundsBeforeRestart=").append(numberRoundsBeforeRestart);
        sb.append('}');
        return sb.toString();
    }
}