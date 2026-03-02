package com.betsoft.casino.bots.strategies;

import com.abs.casino.common.util.RNG;

public class MonotonousShootingStrategy implements IRoomBotStrategy {
    private static final int MIN_WAIT_BETWEEN_ACTIONS = 200;
    private static final int MAX_WAIT_BETWEEN_ACTIONS = 300;

    private long lockTime = 0;
    private int shots = 0;

    @Override
    public int getShots() {
        return shots;
    }

    @Override
    public boolean shouldShoot(String botId) {
        return shots > 0;
    }

    @Override
    public long getWaitTime() {
        long time = System.currentTimeMillis();
        if (time < lockTime) {
            return lockTime - time;
        }
        return RNG.nextInt(MIN_WAIT_BETWEEN_ACTIONS, MAX_WAIT_BETWEEN_ACTIONS);
    }

    @Override
    public int getBuyInAmmoAmount(long balance, float stake, int minAmmo) {
        int maxAmmo = (int) Math.floor(balance / stake);
        if (maxAmmo < minAmmo) {
            return 0;
        } else {
            return RNG.nextInt(minAmmo, maxAmmo);
        }
    }

    @Override
    public void resetShots() {
        shots = 0;
    }

    @Override
    public void resetWeapons() {

    }

    @Override
    public int getShotsForWeapon(int weaponId) {
        return 0;
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
    public void addWeapon(int id, int shots) {
        this.shots += shots;
    }

    @Override
    public int getWeaponId() {
        return -1;
    }

    @Override
    public void consumeAmmo(int weaponId) {
        shots--;
    }

    @Override
    public boolean shouldPurchaseBullets() {
        return shots == 0;
    }
}
