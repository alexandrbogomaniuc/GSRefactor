package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.abs.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BattleGroundSectorXStrategy implements IRoomBotStrategy {
    private static final int MIN_WAIT_BETWEEN_ACTIONS = 1000;
    private static final int MAX_WAIT_BETWEEN_ACTIONS = 4000;
    private static final Logger LOG = LogManager.getLogger(BattleGroundSectorXStrategy.class);
    private static final long LOCK_TIME = 0;
    private int shots = 0;
    private int activeWeaponId = -1;
    private final int stakesLimit;
    private final Map<Integer, Integer> weapons = new HashMap<>();
    private int requestedBetLevel = 1;
    private boolean allowedUseDroppedSW;
    private long requestedByInAmount;

    public BattleGroundSectorXStrategy(int stakesLimit, int requestedBetLevel, boolean allowedUseDroppedSW, long requestedByInAmount) {
        this.stakesLimit = stakesLimit;
        this.requestedBetLevel = requestedBetLevel;
        this.allowedUseDroppedSW = allowedUseDroppedSW;
        this.requestedByInAmount = requestedByInAmount;
        weapons.put(-1, 0);
        for (SpecialWeaponType type : SpecialWeaponType.values()) {
            if (type.getAvailableGameIds().contains((int) GameType.BG_SECTOR_X.getGameId())
                    && !type.isInternalServerShot())
                weapons.put(type.getId(), 0);
        }
    }


    @Override
    public long requestedByInAmount() {
        return requestedByInAmount;
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
    public void activateWeapon(int weaponId) {
        activeWeaponId = weaponId;
    }

    @Override
    public void resetWeapons() {
        //not need
    }

    @Override
    public boolean shouldShoot(String botId) {
        try {
            Thread.sleep(RNG.nextInt(MIN_WAIT_BETWEEN_ACTIONS, MAX_WAIT_BETWEEN_ACTIONS));
        } catch (InterruptedException e) {
            LOG.error(e);
        }
        LOG.debug("BattleGroundSectorXStrategy shouldShoot: activeWeaponId:{}, shots: {}", activeWeaponId, shots);
        return true;
    }

    @Override
    public long getWaitTime() {
        long time = System.currentTimeMillis();
        if (time < LOCK_TIME) {
            return LOCK_TIME - time;
        }
        shots = 1000;
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

    @Override
    public boolean shouldPurchaseWeaponLootBox() {
        return false;
    }

    @Override
    public boolean botHasSpecialWeapons() {
        boolean res = false;
        for (Map.Entry<Integer, Integer> sw : weapons.entrySet()) {
            Integer id = sw.getKey();
            Integer shotsFromSW = sw.getValue();
            if (id >= 0 && shotsFromSW > 0) {
                res = true;
                break;
            }
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
        if (!allowedUseDroppedSW) {
            return false;
        }
        boolean hasSpecialWeapons = botHasSpecialWeapons();
        boolean res = false;

        boolean needUpdateFreeWeapon = hasSpecialWeapons && (activeWeaponId == -1 || weapons.get(activeWeaponId) == 0);
        LOG.debug("BattleGroundSectorXStrategy shouldSwitchWeapon activeWeaponId {}, " +
                        " hasSpecialWeapons: {}, needUpdateFreeWeapon: {}",
                activeWeaponId,  hasSpecialWeapons, needUpdateFreeWeapon);

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
        LOG.debug("shouldSwitchWeapon end , activeWeaponId: {}, needSwitchWeapon: {}", activeWeaponId, res);
        return res;
    }

    @Override
    public boolean shouldPurchaseBullets() {
        return false;
    }

    @Override
    public int requestedBetLevel() {
        return requestedBetLevel;
    }

    public long getRequestedByInAmount() {
        return requestedByInAmount;
    }

    public void setRequestedByInAmount(long requestedByInAmount) {
        this.requestedByInAmount = requestedByInAmount;
    }

    @Override
    public String toString() {
        return "BattleGroundSectorXStrategy{" + "LOG=" + LOG +
                ", lockTime=" + LOCK_TIME +
                ", shots=" + shots +
                ", activeWeaponId=" + activeWeaponId +
                ", stakesLimit=" + stakesLimit +
                ", weapons=" + weapons +
                ", requestedBetLevel=" + requestedBetLevel +
                ", allowedUseDroppedSW=" + allowedUseDroppedSW +
                '}';
    }
}
