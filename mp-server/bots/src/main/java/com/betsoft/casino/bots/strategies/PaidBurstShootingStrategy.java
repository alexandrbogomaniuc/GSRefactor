package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.dgphoenix.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaidBurstShootingStrategy implements IRoomBotStrategy {
    private static final int MIN_WAIT_BETWEEN_ACTIONS = 50;
    private static final int MAX_WAIT_BETWEEN_ACTIONS = 500;
    private Logger LOG = LogManager.getLogger(BurstShootingStrategy.class);
    private long lockTime = 0;
    private int shots = 0;
    private int activeWeaponId = -1;
    private int stakesLimit;
    private Map<Integer, Integer> weapons = new HashMap<>();
    private static final Map<Integer, Double> paidWeaponCosts = new HashMap<>();
    ;


    static {
        try {
            paidWeaponCosts.put(SpecialWeaponType.HolyArrows.getId(), 1.6225677219999954);
            paidWeaponCosts.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(), 2.497003781999993);
            paidWeaponCosts.put(SpecialWeaponType.Bomb.getId(), 16.485018907999955);
            paidWeaponCosts.put(SpecialWeaponType.Landmines.getId(), 18.669345889999946);
            paidWeaponCosts.put(SpecialWeaponType.RocketLauncher.getId(), 9.054273199999974);
            paidWeaponCosts.put(SpecialWeaponType.MachineGun.getId(), 7.743026489999978);
            paidWeaponCosts.put(SpecialWeaponType.Ricochet.getId(), 12.375124856999964);
            paidWeaponCosts.put(SpecialWeaponType.Flamethrower.getId(), 31.78224314999991);
            paidWeaponCosts.put(SpecialWeaponType.Cryogun.getId(), 42.71513244799988);
            paidWeaponCosts.put(SpecialWeaponType.Plasma.getId(), 9.054273199999974);
            paidWeaponCosts.put(SpecialWeaponType.Railgun.getId(), 10.803526116999969);
            paidWeaponCosts.put(SpecialWeaponType.ArtilleryStrike.getId(), 30.469961520999913);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PaidBurstShootingStrategy(int stakesLimit) {
        this.stakesLimit = stakesLimit;
        weapons.put(-1, 0);
        for (SpecialWeaponType type : SpecialWeaponType.values()) {
            if (type.getAvailableGameIds().contains(779) && !type.isInternalServerShot())
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
        LOG.debug("shouldShoot: activeWeaponId:{}, shots: {}", activeWeaponId, shots);
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
            return maxAmmo > 1000 ? 1000 : maxAmmo;
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
    public boolean botHasSpecialWeapons() {
        boolean res = false;
        for (Map.Entry<Integer, Integer> sw : weapons.entrySet()) {
            Integer id = sw.getKey();
            Integer shots = sw.getValue();
            if (id >= 0 && shots > 0)
                res = true;
        }
        return res;
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
        if (weapons.get(weaponId) > 0)
            weapons.put(weaponId, weapons.get(weaponId) - 1);
    }

    @Override
    public void updateWeapon(int weaponId, int shots) {
        weapons.put(weaponId, shots);
    }

    @Override
    public boolean shouldSwitchWeapon() {
        boolean res = false;
        boolean notFoundUsedRealSpecialWeapon = false;
        for (Map.Entry<Integer, Integer> entry : weapons.entrySet()) {
            if (entry.getKey() >= 0 && entry.getValue() > 0)
                notFoundUsedRealSpecialWeapon = true;
        }

        LOG.debug("shouldSwitchWeapon weapons: " + weapons + " activeWeaponId: " + activeWeaponId
                + "  foundNotUsedRealSpecialWeapon: " + notFoundUsedRealSpecialWeapon);

        if (!notFoundUsedRealSpecialWeapon) {
            LOG.debug("shouldSwitchWeapon, activeWeaponId set to CryoGun");
            res = false;
            if (activeWeaponId != 10) {
                activeWeaponId = 10;
                res = true;
            }
        } else {

            if (activeWeaponId >= 0 && weapons.get(activeWeaponId) > 0)
                return false;

            List<Integer> ids = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : weapons.entrySet()) {
                if (entry.getValue() > 0) {
                    if(entry.getKey()!=-1)
                        ids.add(entry.getKey());
                }
            }

            if (ids.size() > 0) {
                int oldWeaponId = activeWeaponId;
                activeWeaponId = ids.get(RNG.nextInt(ids.size()));
                res = oldWeaponId != activeWeaponId;
            }
        }

        LOG.debug("shouldSwitchWeapon end , activeWeaponId: {}, needSwitchWeapon: {}", activeWeaponId, res);
        return res;
    }

    @Override
    public boolean shouldPurchaseBullets() {
        if (activeWeaponId >= 0 && getShotsForWeapon(activeWeaponId) == 0) {
            int multiplierPaidWeapons = getMultiplierPaidWeapons(SpecialWeaponType.values()[activeWeaponId]);
            Integer ammoAmount = weapons.get(-1);
            LOG.debug("shouldPurchaseBullets,  activeWeaponId: {}, multiplierPaidWeapons: {}, shots: {}",
                    activeWeaponId, multiplierPaidWeapons, ammoAmount);
            if (ammoAmount < multiplierPaidWeapons) {
                return true;
            }
        }
        return activeWeaponId == -1 && weapons.get(-1) < stakesLimit;
    }

    public static int getMultiplierPaidWeapons(SpecialWeaponType specialWeaponType) {
        Double mult = paidWeaponCosts.get(specialWeaponType.getId());
        return mult == null ? 0 : new BigDecimal(mult).setScale(0, RoundingMode.UP).intValue() + 1;
    }

}

