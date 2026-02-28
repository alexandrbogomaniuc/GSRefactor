package com.betsoft.casino.mp.model.playerinfo;

import com.abs.casino.common.cache.data.currency.ICurrency;
import com.betsoft.casino.mp.model.*;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Map;
import java.util.StringJoiner;

public class CrashGameBGRoomPlayerInfo extends AbstractBattlegroundRoomPlayerInfo implements ICrashGameRoomPlayerInfo {
    private static final byte VERSION = 0;

    public CrashGameBGRoomPlayerInfo() {}

    /**
     * @param id accountId
     * @param bankId id of bank
     * @param roomId current roomId
     * @param seatNumber seat number of player in room
     * @param sessionId player's sessionId
     * @param gameSessionId game sessionId
     * @param nickname player's nickname
     * @param avatar avatar for client
     * @param enterDate enter date to room
     * @param currency player's currency
     * @param stats common stats for player
     * @param showRefreshButton is refresh button showed or not
     * @param weapons weapons of player
     * @param stake stake of player in room. Should be equal to the stake in the room.
     * @param stakesReserve number of stakes to be transferred from balance
     * @param weaponMode weapon mode, lootbox or paid shots.
     * @param allowWeaponSaveInAllGames allow save weapons between rounds.
     * @param battlegroundRake battleground rake in percent
     */
    public CrashGameBGRoomPlayerInfo(long id, long bankId, long roomId, int seatNumber, String sessionId, long gameSessionId, String nickname,
                                     IAvatar avatar, long enterDate, ICurrency currency, IPlayerStats stats, boolean showRefreshButton,
                                     Map<Integer, Integer> weapons, long stake, int stakesReserve,
                                     MaxQuestWeaponMode weaponMode, boolean allowWeaponSaveInAllGames, double battlegroundRake) {
        super(id, bankId, roomId, seatNumber, sessionId, gameSessionId, nickname, avatar, enterDate, currency, stats, showRefreshButton,
                weapons, null, stake, stakesReserve, weaponMode, allowWeaponSaveInAllGames, battlegroundRake, false, false);
    }


    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeByte(VERSION);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        input.readByte();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashGameBGRoomPlayerInfo.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("bankId=" + bankId)
                .add("roomId=" + roomId)
                .add("seatNumber=" + seatNumber)
                .add("sessionId='" + sessionId + "'")
                .add("gameSessionId=" + gameSessionId)
                .add("nickname='" + nickname + "'")
                .add("enterDate=" + enterDate)
                .add("currency=" + currency)
                .add("wantSitOut=" + wantSitOut)
                .add("avatar=" + avatar)
                .add("showRefreshButton=" + showRefreshButton)
                .add("externalRoundId=" + externalRoundId)
                .add("roundBuyInAmount=" + roundBuyInAmount)
                .add("prevXP=" + prevXP)
                .add("pendingOperation=" + pendingOperation)
                .add("lastOperationInfo='" + lastOperationInfo + "'")
                .add("stats=" + stats)
                .add("roundStats=" + roundStats)
                .add("activeFrbSession=" + activeFrbSession)
                .add("activeCashBonusSession=" + activeCashBonusSession)
                .add("tournamentSession=" + tournamentSession)
                .add("playerQuests=" + playerQuests)
                .add("stake=" + stake)
                .add("stakesReserve=" + stakesReserve)
                .add("buyInCount=" + buyInCount)
                .toString();
    }


    @Override
    protected AbstractRoomPlayerInfo getDeserialize() {
        return this;
    }
}
