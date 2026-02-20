package com.betsoft.casino.mp.model.playerinfo;

import com.betsoft.casino.mp.model.*;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.StringJoiner;

public class CrashGameRoomPlayerInfo extends AbstractRoomPlayerInfo implements ICrashGameRoomPlayerInfo {
    private static final byte VERSION = 0;

    public CrashGameRoomPlayerInfo() {}

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
     * @param playerQuests player quests in room (not used in new games)
     * @param stake stake of player in room. Should be equal to the stake in the room.
     * @param stakesReserve number of stakes to be transferred from balance
     */
    public CrashGameRoomPlayerInfo(long id, long bankId, long roomId, int seatNumber, String sessionId,
                                   long gameSessionId, String nickname, IAvatar avatar, long enterDate,
                                   ICurrency currency, IPlayerStats stats, boolean showRefreshButton,
                                   IPlayerQuests playerQuests, long stake, int stakesReserve) {
        super(id, bankId, roomId, seatNumber, sessionId, gameSessionId, nickname, avatar, enterDate, currency,
                stats, showRefreshButton, playerQuests, stake, stakesReserve);
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
        return new StringJoiner(", ", CrashGameRoomPlayerInfo.class.getSimpleName() + "[", "]")
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
    protected void serializeAdditional(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
    }

    @Override
    protected void deserializeAdditional(JsonParser p, JsonNode node, DeserializationContext ctxt)
            throws IOException {
        
    }

    @Override
    protected AbstractRoomPlayerInfo getDeserialize() {
        return this;
    }
}
