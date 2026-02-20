package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.bots.ILobbyBot;
import com.betsoft.casino.mp.common.GameMapShape;
import com.betsoft.casino.mp.model.IRoomEnemy;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.transport.PointExt;
import com.betsoft.casino.mp.transport.RoomEnemy;

import java.util.List;

public interface IRoomBotStrategy {
    int WEAPON_SWITCH_TIME = 3000;
    int MIN_SHOT_PAUSE = 300;
    int MAX_SHOT_PAUSE = 500;

    int getShots();
    void resetShots();
    boolean shouldShoot(String botId);
    default boolean shouldSendBullet(String botId) { return true; }
    boolean shouldPurchaseWeaponLootBox();
    boolean shouldSwitchWeapon();
    boolean shouldPurchaseBullets();
    long getWaitTime();
    int getBuyInAmmoAmount(long balance, float stake, int minAmmo);
    void addWeapon(int id, int shots);
    default void updateWeapon(int id, int shots){}
    int getWeaponId();
    void consumeAmmo(int weaponId);
    default void activateWeapon(int weaponId) {}

    void resetWeapons();
    int getShotsForWeapon(int weaponId);

    default void setLobbyBot(ILobbyBot lobbyBot) {}
    default ILobbyBot getLobbyBot() { return null; }

    default boolean needSitOutFromRoom(){
        return false;
    }

    default int requestedBetLevel(){return  1;}

    default boolean botHasSpecialWeapons() {return false;}

    default long requestedByInAmount(){ return 0;}

    default boolean isLocationOnMapAllowedForShot(List<PointExt> points, long serverTime, int currentMapId, int enemyType,
                                                  Point serverLocationByEnemy, GameMapShape map) {
        return true;
    }

    default Point getLocationOnScreen(RoomEnemy roomEnemy, long serverTime) {
        return null;
    }

    default boolean allowShotAfterRoundFinishSoon(){
        return true;
    }

    default boolean isSpecialCaseForEnemy(IRoomEnemy randomFirstEnemy){
        return false;
    }

    default Long[] getRequestedEnemiesIds() {
        return null;
    }
    default IRoomEnemy getEnemyToShoot(List<RoomEnemy> enemies) {
        return null;
    }

    static Long[] convertIds(String requestedEnemyIds) {
        if (requestedEnemyIds != null && !requestedEnemyIds.isEmpty()) {
            String[] ids = requestedEnemyIds.split(",");
            Long[] idsLong = new Long[ids.length];
            for (int i = 0; i < ids.length; i++) {
                try {
                    idsLong[i] = Long.parseLong(ids[i]);
                } catch (NumberFormatException e) {
                    idsLong[i] = null;
                }
            }
            // Count non-null elements
            int count = 0;
            for (Long id : idsLong) {
                if (id != null) {
                    count++;
                }
            }
            // Create a new array with non-null elements
            Long[] nonNullIds = new Long[count];
            int index = 0;
            for (Long id : idsLong) {
                if (id != null) {
                    nonNullIds[index++] = id;
                }
            }
            return nonNullIds;
        } else {
            return null;
        }
    }

    static boolean contains(Long[] requestedIds, Long id) {
        for (Long requestedId : requestedIds) {
            if (requestedId != null && requestedId.equals(id)) {
                return true;
            }
        }
        return false;
    }
}

