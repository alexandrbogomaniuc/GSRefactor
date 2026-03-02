package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.abs.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CogStrategyWithWeaponId implements IRoomBotStrategy {
    private static final int MIN_WAIT_BETWEEN_ACTIONS = 1000;
    private static final int MAX_WAIT_BETWEEN_ACTIONS = 4000;
    private Logger LOG = LogManager.getLogger(Ra2PaidShotStrategyWithId.class);
    private long lockTime = 0;
    private int shots = 0;
    private int activeWeaponId = -1;
    private int stakesLimit;
    private Map<Integer, Integer> weapons = new HashMap<>();
    private static final Map<Integer, Integer> paidWeaponCosts = new HashMap<>();
    private int requestedWeaponPaidId = -2;
    private int requestedBetLevel = 1;



    static {
        try {
            paidWeaponCosts.put(SpecialWeaponType.Ricochet.getId(), 30);
            paidWeaponCosts.put(SpecialWeaponType.Lightning.getId(), 60);
            paidWeaponCosts.put(SpecialWeaponType.Napalm.getId(), 90);
            paidWeaponCosts.put(SpecialWeaponType.ArtilleryStrike.getId(), 120);
            paidWeaponCosts.put(SpecialWeaponType.Nuke.getId(), 150);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CogStrategyWithWeaponId(int stakesLimit, int requestedWeaponPaidId, int requestedBetLevel) {
        this.stakesLimit = stakesLimit;
        this.requestedWeaponPaidId = requestedWeaponPaidId;
        this.requestedBetLevel = requestedBetLevel;

        weapons.put(-1, 0);
        for (SpecialWeaponType type : SpecialWeaponType.values()) {
            if (type.getAvailableGameIds().contains((int) GameType.CLASH_OF_THE_GODS.getGameId())
                    && !type.isInternalServerShot())
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
        try {
            if(requestedWeaponPaidId == SpecialWeaponType.Lightning.getId() ||
                    requestedWeaponPaidId == SpecialWeaponType.Ricochet.getId()){
                Thread.sleep(1000);
            }
            if(requestedWeaponPaidId == SpecialWeaponType.Napalm.getId()
                    || requestedWeaponPaidId == SpecialWeaponType.ArtilleryStrike.getId()
                    || requestedWeaponPaidId == SpecialWeaponType.Nuke.getId()
            ) {
                Thread.sleep(RNG.nextInt(MIN_WAIT_BETWEEN_ACTIONS, MAX_WAIT_BETWEEN_ACTIONS));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.debug("Ra2PaidShotStrategyWithId shouldShoot: activeWeaponId:{}, shots: {}", activeWeaponId, shots);
        return true;
    }

    @Override
    public long getWaitTime() {
        long time = System.currentTimeMillis();
        if (time < lockTime) {
            return lockTime - time;
        }
        shots = 1000;
        return RNG.nextInt(200, 300);
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
        boolean hasSpecialWeapons = botHasSpecialWeapons();
        boolean res = false;

        boolean needUpdateFreeWeapon = hasSpecialWeapons && (activeWeaponId == -1 || weapons.get(activeWeaponId) == 0);
        LOG.debug("Ra2PaidShotStrategyWithId shouldSwitchWeapon activeWeaponId {}, requestedWeaponPaidId: {}," +
                        " hasSpecialWeapons: {}, needUpdateFreeWeapon: {}",
                activeWeaponId, requestedWeaponPaidId, hasSpecialWeapons, needUpdateFreeWeapon);

        if(needUpdateFreeWeapon){
            Optional<Map.Entry<Integer, Integer>> firstNotEmptyWeapon = weapons.entrySet().stream()
                    .filter(weapon -> (weapon.getValue() > 0 && weapon.getKey()!=-1))
                    .findFirst();

            firstNotEmptyWeapon.ifPresent(
                    integerIntegerEntry -> activeWeaponId = integerIntegerEntry.getKey());

            LOG.debug("shouldSwitchWeapon found free weapons need switch, activeWeaponId: {}, " +
                    "firstNotEmptyWeapon: {}", activeWeaponId, firstNotEmptyWeapon);
            return true;
        }

        if (!hasSpecialWeapons && activeWeaponId != requestedWeaponPaidId) {
            activeWeaponId = requestedWeaponPaidId;
            res = true;
        }
        LOG.debug("shouldSwitchWeapon end , activeWeaponId: {}, needSwitchWeapon: {}", activeWeaponId, res);
        return res;
    }

    @Override
    public boolean shouldPurchaseBullets() {
        if (activeWeaponId >= 0 && getShotsForWeapon(activeWeaponId) == 0) {
            int multiplierPaidWeapons = getMultiplierPaidWeapons(SpecialWeaponType.values()[activeWeaponId]);
            Integer ammoAmount = weapons.get(-1);
            LOG.debug("shouldPurchaseBullets,  activeWeaponId: {}, multiplierPaidWeapons: {}, shots: {}, requestedBetLevel: {}",
                    activeWeaponId, multiplierPaidWeapons, ammoAmount, requestedBetLevel);
            if (ammoAmount < (multiplierPaidWeapons * requestedBetLevel)) {
                return true;
            }
        }
        return activeWeaponId == -1 && weapons.get(-1) < stakesLimit;
    }

    @Override
    public boolean needSitOutFromRoom() {
        return RNG.nextInt(1000) > 960;
    }


    public static int getMultiplierPaidWeapons(SpecialWeaponType specialWeaponType) {
        Integer mult = paidWeaponCosts.get(specialWeaponType.getId());
        return mult == null ? 0 : mult;
    }

    @Override
    public int requestedBetLevel() {
        return requestedBetLevel;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CogStrategyWithWeaponId{");
        sb.append("LOG=").append(LOG);
        sb.append(", lockTime=").append(lockTime);
        sb.append(", shots=").append(shots);
        sb.append(", activeWeaponId=").append(activeWeaponId);
        sb.append(", stakesLimit=").append(stakesLimit);
        sb.append(", weapons=").append(weapons);
        sb.append(", requestedWeaponPaidId=").append(requestedWeaponPaidId);
        sb.append(", requestedBetLevel=").append(requestedBetLevel);
        sb.append('}');
        return sb.toString();
    }
}
