package com.betsoft.casino.mp.model.playerinfo;

import com.abs.casino.common.cache.data.currency.ICurrency;
import com.betsoft.casino.mp.model.IAvatar;
import com.betsoft.casino.mp.model.IPlayerStats;
import com.betsoft.casino.mp.model.MaxQuestWeaponMode;

public class BattlegroundRoomPlayerInfo extends AbstractBattlegroundRoomPlayerInfo {

    public BattlegroundRoomPlayerInfo() {}

    /**
     *
     * @param id accountId
     * @param bankId bankId
     * @param roomId current roomId
     * @param seatNumber seat number of player in room
     * @param sessionId player sessionId
     * @param gameSessionId game sessionId
     * @param avatar avatar for client
     * @param enterDate enter date to room
     * @param currency currency of player
     * @param stats common stats for player
     * @param stake stake of player in room. Should be equal to the stake in the room.
     * @param stakesReserve number of stakes to be transferred from balance
     * @param weaponMode weapon mode, lootbox or paid shots.
     * @param allowWeaponSaveInAllGames allow save weapons between rounds.
     */
    public BattlegroundRoomPlayerInfo(long id, long bankId, long roomId, int seatNumber, String sessionId, long gameSessionId,
                                      String nickname, IAvatar avatar, long enterDate, ICurrency currency, IPlayerStats stats,
                                      boolean showRefreshButton,
                                      long stake, int stakesReserve, MaxQuestWeaponMode weaponMode, boolean allowWeaponSaveInAllGames,
                                      double battlegroundRake, boolean privateRoom, boolean roomManager) {
        super(id, bankId, roomId, seatNumber, sessionId, gameSessionId, nickname, avatar, enterDate, currency, stats, showRefreshButton,
                null, null, stake, stakesReserve, weaponMode, allowWeaponSaveInAllGames,
                battlegroundRake, privateRoom, roomManager);
    }

    @Override
    protected AbstractRoomPlayerInfo getDeserialize() {
        return this;
    }
}
