package com.betsoft.casino.mp.model.playerinfo;

import com.betsoft.casino.mp.model.*;

import java.util.Map;

public class DefaultRoomPlayerInfo extends AbstractActionRoomPlayerInfo {

    public DefaultRoomPlayerInfo() {}

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
     * @param weapons
     * @param playerQuests player quests in room (not used in new games)
     * @param stake stake of player in room. Should be equal to the stake in the room.
     * @param stakesReserve number of stakes to be transferred from balance
     * @param weaponMode weapon mode, lootbox or paid shots.
     * @param allowWeaponSaveInAllGames allow save weapons between rounds.
     */
    public DefaultRoomPlayerInfo(long id, long bankId, long roomId, int seatNumber, String sessionId, long gameSessionId,
                                 String nickname, IAvatar avatar, long enterDate, ICurrency currency, IPlayerStats stats,
                                 boolean showRefreshButton, Map<Integer, Integer> weapons, IPlayerQuests playerQuests,
                                 long stake, int stakesReserve, MaxQuestWeaponMode weaponMode, boolean allowWeaponSaveInAllGames) {
        super(id, bankId, roomId, seatNumber, sessionId, gameSessionId, nickname, avatar, enterDate, currency, stats,
                showRefreshButton, weapons, playerQuests, stake, stakesReserve, weaponMode, allowWeaponSaveInAllGames);
    }

    @Override
    protected AbstractRoomPlayerInfo getDeserialize() {
        return this;
    }
}
