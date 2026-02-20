package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.bots.ILobbyBot;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.LobbyBot;
import com.betsoft.casino.bots.RoomBot;
import com.betsoft.casino.mp.sectorx.model.math.EnemyType;
import com.betsoft.casino.mp.model.IRoomEnemy;
import com.betsoft.casino.mp.transport.RoomEnemy;
import com.dgphoenix.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SectorXStrategy implements IRoomBotStrategy {

    public static final int ENEMY_TYPE_ID_SECTORX_CONCEALED_COINS = EnemyType.B1.getId();
    private static final int MIN_WAIT_BETWEEN_ACTIONS = 1000;
    private static final int MAX_WAIT_BETWEEN_ACTIONS = 4000;
    private static final int DEFAULT_WEAPON_ID = -1;
    private static final Logger LOG = LogManager.getLogger(SectorXStrategy.class);
    private static final long LOCK_TIME = 0;
    private int shots = 0;
    private int activeWeaponId = DEFAULT_WEAPON_ID;
    private final int stakesLimit;
    private final Map<Integer, Integer> weapons = new HashMap<>();
    private final int requestedBetLevel;
    private final Long[] requestedEnemyTypeIds;

    public SectorXStrategy(int stakesLimit, int requestedBetLevel, String requestedEnemyTypeIds) {
        this.stakesLimit = stakesLimit;
        this.requestedBetLevel = requestedBetLevel;
        this.requestedEnemyTypeIds = IRoomBotStrategy.convertIds(requestedEnemyTypeIds);

        weapons.put(DEFAULT_WEAPON_ID, 0);
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
        LOG.debug("SectorXStrategy shouldShoot: activeWeaponId:{}, shots: {}", activeWeaponId, shots);
        return true;
    }

    public IRoomEnemy getEnemyToShoot(List<RoomEnemy> enemies) {
        if (enemies != null) {
            List<RoomEnemy> requestedEnemies = enemies.stream()
                    .filter(enemy -> IRoomBotStrategy.contains(requestedEnemyTypeIds, enemy.getTypeId()))
                    .collect(Collectors.toList());

            if (requestedEnemies.size() > 0) {
                RoomEnemy requestedEnemy = requestedEnemies.get(RNG.nextInt(requestedEnemies.size()));
                IRoomEnemy concealedEnemy = null;
                if (requestedEnemy != null && requestedEnemy.getTypeId() == ENEMY_TYPE_ID_SECTORX_CONCEALED_COINS) {
                    concealedEnemy = tryFindConcealedEnemy(enemies, requestedEnemy.getId());
                }
                return concealedEnemy == null ? requestedEnemy : concealedEnemy;
            } else {

                //try to return random enemy from the current bot state
                ILobbyBot lobbyBot = getLobbyBot();
                if(lobbyBot instanceof LobbyBot) {
                    IRoomBot roomBot = ((LobbyBot) lobbyBot).getRoomBot();
                    if(roomBot instanceof RoomBot) {
                        return ((RoomBot) roomBot).getRandomEnemy();
                    }
                }
            }
        }
        return null;
    }

    private IRoomEnemy tryFindConcealedEnemy(List<RoomEnemy> enemies, Long parentEnemyId) {
        if(enemies != null) {
            List<RoomEnemy> concealedEnemies = enemies.stream()
                    .filter(cEnemy -> cEnemy.getParentEnemyId() == parentEnemyId
                            && cEnemy.getParentEnemyTypeId() == ENEMY_TYPE_ID_SECTORX_CONCEALED_COINS)
                    .collect(Collectors.toList());

            if(concealedEnemies.size() > 0) {
                return concealedEnemies.get(RNG.nextInt(concealedEnemies.size()));
            }
        }
        return null;
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
        if (weapons.get(weaponId) > 0)
            weapons.put(weaponId, weapons.get(weaponId) - 1);
    }

    @Override
    public void updateWeapon(int weaponId, int shots) {
        weapons.put(weaponId, shots);
    }

    @Override
    public boolean shouldSwitchWeapon() {
        return false;
    }

    @Override
    public boolean shouldPurchaseBullets() {
        return activeWeaponId == DEFAULT_WEAPON_ID && weapons.get(DEFAULT_WEAPON_ID) < stakesLimit;
    }

    @Override
    public int requestedBetLevel() {
        return requestedBetLevel;
    }

    @Override
    public Long[] getRequestedEnemiesIds() {
        return requestedEnemyTypeIds;
    }

    @Override
    public String toString() {
        return "SectorXStrategy{" +
                "shots=" + shots +
                ", activeWeaponId=" + activeWeaponId +
                ", stakesLimit=" + stakesLimit +
                ", weapons=" + weapons +
                ", requestedBetLevel=" + requestedBetLevel +
                '}';
    }
}
