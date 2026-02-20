package com.betsoft.casino.bots.strategies;

import com.dgphoenix.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SpecialShootingStrategy implements IRoomBotStrategy {
    private Logger LOG = LogManager.getLogger(SpecialShootingStrategy.class);
    private long lockTime = 0;
    private int shots = 0;
    private int activeWeaponId = -1;
    private int purchasedWeaponId = -1;

    public SpecialShootingStrategy() {
    }

    @Override
    public int getShots() {
        return shots;
    }

    @Override
    public void resetShots() {
        shots = 0;
    }

    @Override
    public boolean shouldShoot(String botId) {
        return activeWeaponId != -1 && shots > 0;
    }

    @Override
    public long getWaitTime() {
        long time = System.currentTimeMillis();
        if (time < lockTime) {
            return lockTime - time;
        }
        return RNG.nextInt(MIN_SHOT_PAUSE, MAX_SHOT_PAUSE);
    }

    @Override
    public int getBuyInAmmoAmount(long balance, float stake, int minAmmo) {
        return 1000;
    }

    @Override
    public boolean shouldPurchaseWeaponLootBox() {
        return shots == 0;
    }

    @Override
    public void addWeapon(int id, int shots) {
        if (id != -1) {
            purchasedWeaponId = id;
            this.shots += shots;
        }
    }

    @Override
    public int getWeaponId() {
        return purchasedWeaponId;
    }

    @Override
    public void consumeAmmo(int weaponId) {
        shots--;
    }

    @Override
    public boolean shouldSwitchWeapon() {
        return purchasedWeaponId != activeWeaponId;
    }

    @Override
    public boolean shouldPurchaseBullets() {
        return false;
    }

    @Override
    public void activateWeapon(int weaponId) {
        activeWeaponId = weaponId;
    }

    @Override
    public void resetWeapons() {
    }

    @Override
    public int getShotsForWeapon(int weaponId) {
        return 0;
    }
}
