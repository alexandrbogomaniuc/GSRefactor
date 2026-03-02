package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.abs.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PiratesWithoutBuyInSpecialWeapons implements IRoomBotStrategy {
    private static final int MIN_WAIT_BETWEEN_ACTIONS = 50;
    private static final int MAX_WAIT_BETWEEN_ACTIONS = 500;
    private Logger LOG = LogManager.getLogger(PiratesWithoutBuyInSpecialWeapons.class);
    private long lockTime = 0;
    private int shots = 0;
    private int activeWeaponId = -1;
    private int stakesLimit;
    private Map<Integer, Integer> weapons = new HashMap<>();

    public PiratesWithoutBuyInSpecialWeapons(int stakesLimit) {
        this.stakesLimit = stakesLimit;
        weapons.put(-1, 0);
        for (SpecialWeaponType type : SpecialWeaponType.values()) {
            weapons.put(type.getId(), 0);
        }
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
    public void resetWeapons() {

    }

    @Override
    public void activateWeapon(int weaponId) {
        activeWeaponId = weaponId;
    }

    @Override
    public boolean shouldShoot(String botId) {
        if (activeWeaponId >= 0 && weapons.get(activeWeaponId) > 0 && shots > 0) {
            shots--;
            return true;
        }
        if (shots > 0) {
            shots--;
            return true;
        }
        return false;
    }

    @Override
    public long getWaitTime() {
        long time = System.currentTimeMillis();
        if (time < lockTime) {
            return lockTime - time;
        }
        if (shots == 0) {
            shots = RNG.nextInt(2, 12);
        }
        return RNG.nextInt(MIN_SHOT_PAUSE, MAX_SHOT_PAUSE);
    }

    @Override
    public int getBuyInAmmoAmount(long balance, float stake, int minAmmo) {
        int maxAmmo = (int) Math.floor(balance / stake);
        LOG.debug("getBuyInAmmoAmount: balance={}, stake={}, minAmmo={}, maxAmmo={}", balance, stake, minAmmo, maxAmmo);
        if (maxAmmo < minAmmo) {
            return 0;
        } else {
            return Math.min(maxAmmo, 1000);
        }
    }

    private void lock(int lockTimeMs) {
        lockTime = System.currentTimeMillis() + lockTimeMs;
    }

    @Override
    public boolean shouldPurchaseWeaponLootBox() {
        return false;
    }

    @Override
    public int getShotsForWeapon(int weaponId) {
        return weapons.get(weaponId);
    }

    @Override
    public void addWeapon(int id, int shots) {
        weapons.put(id, weapons.get(id) + shots);
    }

    @Override
    public int getWeaponId() {
        return activeWeaponId;
    }

    @Override
    public void consumeAmmo(int weaponId) {
        weapons.put(weaponId, weapons.get(weaponId) - 1);
    }

    @Override
    public void updateWeapon(int weaponId, int shots) {
        weapons.put(weaponId, shots);
    }

    @Override
    public boolean shouldSwitchWeapon() {
        if (weapons.get(activeWeaponId).equals(0) || RNG.rand() < 0.1) {
            List<Integer> ids = new ArrayList<>();
            for (int i = -1; i < SpecialWeaponType.values().length; i++) {
                if (weapons.get(i) > 0) {
                    ids.add(i);
                }
            }
            if (ids.size() > 0) {
                int oldWeaponId = activeWeaponId;
                activeWeaponId = ids.get(RNG.nextInt(ids.size()));
                return oldWeaponId != activeWeaponId;
            }
        }
        return false;
    }

    @Override
    public boolean shouldPurchaseBullets() {
        return activeWeaponId == -1 && weapons.get(-1) < stakesLimit;
    }
}

