package com.betsoft.casino.mp.model.playerinfo;

import com.abs.casino.common.cache.data.currency.ICurrency;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.battleground.IBgPlace;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractBattlegroundRoomPlayerInfo extends AbstractActionRoomPlayerInfo
        implements IBattlegroundRoomPlayerInfo {

    private static final byte VERSION = 2;

    private IBattlegroundRoundInfo battlegroundRoundInfo;
    private double battlegroundRake;
    private boolean privateRoom;
    /**
     * flag indicates owner of private room
     */
    private boolean isOwner;
    private String userName;

    public AbstractBattlegroundRoomPlayerInfo() {}

    public AbstractBattlegroundRoomPlayerInfo(long id, long bankId, long roomId, int seatNumber, String sessionId, long gameSessionId,
                                              String nickname, IAvatar avatar, long enterDate, ICurrency currency, IPlayerStats stats,
                                              boolean showRefreshButton, Map<Integer, Integer> weapons, IPlayerQuests playerQuests,
                                              long stake, int stakesReserve, MaxQuestWeaponMode weaponMode, boolean allowWeaponSaveInAllGames,
                                              double battlegroundRake, boolean privateRoom, boolean isOwner) {
        super(id, bankId, roomId, seatNumber, sessionId, gameSessionId, nickname, avatar, enterDate, currency, stats, showRefreshButton, weapons,
                playerQuests, stake, stakesReserve, weaponMode, allowWeaponSaveInAllGames);
        this.battlegroundRake = battlegroundRake;
        this.privateRoom = privateRoom;
        this.isOwner = isOwner;
    }

    @Override
    public void finishCurrentRound() {
        super.finishCurrentRound();
        battlegroundRoundInfo = null;
    }

    @Override
    public IBattlegroundRoundInfo getBattlegroundRoundInfo() {
        return battlegroundRoundInfo;
    }

    @Override
    public void setBattlegroundRoundInfo(IBattlegroundRoundInfo battlegroundRoundInfo) {
        this.battlegroundRoundInfo = battlegroundRoundInfo;
    }

    @Override
    public IBgPlace createBattlegroundRoundInfo(long buyIn, long winAmount, long betsSum, long winSum, String status,
                                                int playersNumber, String winnerName, long accountId, int rank, long gameSessionId,
                                                long gameScore, long roundId, long roundStartDate, double ejectPoint, String privateRoomId) {
        battlegroundRoundInfo = new BattlegroundRoundInfo(buyIn, winAmount, betsSum, winSum, null, status,
                playersNumber, winnerName, roundId, roundStartDate, privateRoomId);
        return new BgPlace(accountId, winAmount, rank, betsSum, winSum, gameSessionId, gameScore, ejectPoint);
    }

    @Override
    public void setBattlegroundRake(double battlegroundRake) {
        this.battlegroundRake = battlegroundRake;
    }

    @Override
    public double getBattlegroundRake() {
        return battlegroundRake;
    }

    public boolean isPrivateRoom() {
        return privateRoom;
    }

    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        this.isOwner = owner;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeByte(VERSION);
        kryo.writeClassAndObject(output, battlegroundRoundInfo);
        output.writeDouble(battlegroundRake);
        output.writeBoolean(privateRoom);
        output.writeBoolean(isOwner);
        output.writeString(userName);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        byte version = input.readByte();
        battlegroundRoundInfo = (IBattlegroundRoundInfo) kryo.readClassAndObject(input);
        battlegroundRake = input.readDouble();
        if (version > 0) {
            privateRoom = input.readBoolean();
            isOwner = input.readBoolean();
        }
        if (version > 1) {
            userName = input.readString();
        }
    }

    protected void serializeAdditional(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        super.serializeAdditional(gen, serializers);

        gen.writeObjectField("battlegroundRoundInfo", battlegroundRoundInfo);
        gen.writeNumberField("battlegroundRake", battlegroundRake);
        gen.writeBooleanField("privateRoom", privateRoom);
        gen.writeBooleanField("isOwner", isOwner);
        gen.writeStringField("userName", userName);
    }

    protected void deserializeAdditional(JsonParser p, JsonNode node, DeserializationContext ctxt) throws IOException {
        super.deserializeAdditional(p, node, ctxt);

        battlegroundRoundInfo = ((ObjectMapper) p.getCodec()).convertValue(node.get("battlegroundRoundInfo"), IBattlegroundRoundInfo.class);
        battlegroundRake = node.get("battlegroundRake").asDouble();
        privateRoom = node.get("privateRoom").asBoolean();
        isOwner = node.get("isOwner").asBoolean();
        userName = readNullableText(node, "userName");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoomPlayerInfo [");
        sb.append("id=").append(id);
        sb.append(", bankId=").append(bankId);
        sb.append(", roomId=").append(roomId);
        sb.append(", seatNumber=").append(seatNumber);
        sb.append(", sessionId=").append(sessionId);
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", enterDate=").append(enterDate);
        sb.append(", currency=").append(currency);
        sb.append(", wantSitOut=").append(wantSitOut);
        sb.append(", avatar=").append(avatar);
        sb.append(", specialWeaponId=").append(specialWeaponId);
        sb.append(", showRefreshButton=").append(showRefreshButton);
        sb.append(", externalRoundId=").append(externalRoundId);
        sb.append(", roundBuyInAmount=").append(roundBuyInAmount);
        sb.append(", pendingOperation=").append(pendingOperation);
        sb.append(", stats=").append(stats);
        sb.append(", roundStats=").append(roundStats);
        sb.append(", activeFrbSession=").append(activeFrbSession);
        sb.append(", activeCashBonusSession=").append(activeCashBonusSession);
        sb.append(", activeTournamentSession=").append(tournamentSession);
        sb.append(", weapons=").append(weapons);
        sb.append(", playerQuests=").append(playerQuests);
        sb.append(", prevXP=").append(prevXP);
        sb.append(", stake=").append(stake);
        sb.append(", stakesReserve=").append(stakesReserve);
        sb.append(", lastOperationInfo='").append(lastOperationInfo).append("'");
        sb.append(", buyInCount=").append(buyInCount);
        sb.append(", battlegroundRoundInfo=").append(battlegroundRoundInfo);
        sb.append(", battleGroundRake=").append(battlegroundRake);
        sb.append(", privateRoom=").append(privateRoom);
        sb.append(", isOwner=").append(isOwner);
        sb.append(", userName=").append(userName);
        sb.append(']');
        return sb.toString();
    }
}
