package com.betsoft.casino.mp.model.playerinfo;

import com.betsoft.casino.mp.model.*;
import com.abs.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;

public abstract class AbstractRoomPlayerInfo<RPI extends AbstractRoomPlayerInfo> implements IRoomPlayerInfo,
        KryoSerializable, JsonSelfSerializable<RPI> {
    private static final byte VERSION = 0;

    protected long id;
    protected long bankId;
    protected long roomId;
    protected int seatNumber;
    protected String sessionId;
    protected long gameSessionId;
    protected String nickname;
    protected long enterDate;
    protected ICurrency currency;
    protected boolean wantSitOut;
    protected IAvatar avatar;
    protected boolean showRefreshButton;
    protected long externalRoundId;
    protected long roundBuyInAmount;
    protected IExperience prevXP = new Experience(0);
    protected boolean pendingOperation = false;
    protected String lastOperationInfo;
    protected IPlayerStats stats;
    protected IPlayerStats roundStats;
    protected IActiveFrbSession activeFrbSession;
    protected IActiveCashBonusSession activeCashBonusSession;
    protected ITournamentSession tournamentSession;
    protected IPlayerQuests playerQuests;
    //stake in cents and playerCurrency
    protected long stake;
    //number of stakes to be transferred from balance
    protected int stakesReserve;
    protected int buyInCount = 0;

    public AbstractRoomPlayerInfo() {
    }

    public AbstractRoomPlayerInfo(long id, long bankId, long roomId, int seatNumber, String sessionId, long gameSessionId,
                          String nickname, IAvatar avatar, long enterDate, ICurrency currency,
                          IPlayerStats stats, boolean showRefreshButton,
                          IPlayerQuests playerQuests, long stake, int stakesReserve) {
        this.id = id;
        this.bankId = bankId;
        this.roomId = roomId;
        this.seatNumber = seatNumber;
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.enterDate = enterDate;
        this.currency = currency;
        this.stats = stats;
        this.roundStats = new PlayerStats();
        this.roundStats.incrementVersion();
        this.showRefreshButton = showRefreshButton;
        this.playerQuests = playerQuests == null ? new PlayerQuests(new HashSet<>()) : playerQuests;
        this.stake = stake;
        this.stakesReserve = stakesReserve;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public long getBankId() {
        return bankId;
    }

    @Override
    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    @Override
    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public int getSeatNumber() {
        return seatNumber;
    }

    @Override
    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    @Override
    public long getGameSessionId() {
        return gameSessionId;
    }

    @Override
    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public long getEnterDate() {
        return enterDate;
    }

    @Override
    public void setEnterDate(long enterDate) {
        this.enterDate = enterDate;
    }

    @Override
    public IExperience getTotalScore() {
        return stats.getScore();
    }

    @Override
    public IExperience getCurrentScore() {
        return roundStats.getScore();
    }

    public ICurrency getCurrency() {
        return currency;
    }

    @Override
    public void setCurrency(ICurrency currency) {
        this.currency = currency;
    }

    @Override
    public boolean isWantSitOut() {
        return wantSitOut;
    }

    @Override
    public void setWantSitOut(boolean wantSitOut) {
        this.wantSitOut = wantSitOut;
    }

    @Override
    public IAvatar getAvatar() {
        return avatar;
    }

    @Override
    public void setAvatar(IAvatar avatar) {
        this.avatar = avatar;
    }

    @Override
    public boolean isPendingOperation() {
        return pendingOperation;
    }

    @Override
    public void setPendingOperation(boolean pendingOperation) {
        this.pendingOperation = pendingOperation;
    }

    @Override
    public void setPendingOperation(boolean pendingOperation, String lastOperationInfo) {
        this.pendingOperation = pendingOperation;
        this.lastOperationInfo = lastOperationInfo;
    }

    @Override
    public String getLastOperationInfo() {
        return lastOperationInfo;
    }

    @Override
    public void setLastOperationInfo(String lastOperationInfo) {
        this.lastOperationInfo = lastOperationInfo;
    }

    @Override
    public IPlayerStats getStats() {
        return stats;
    }

    @Override
    public void setStats(IPlayerStats stats) {
        this.stats = stats;
    }

    @Override
    public IPlayerStats setNewPlayerStats() {
        this.stats = new PlayerStats();
        return this.stats;
    }

    @Override
    public IPlayerStats getRoundStats() {
        return roundStats;
    }

    @Override
    public void setRoundStats(IPlayerStats roundStats) {
        this.roundStats = roundStats;
    }

    @Override
    public void setNewRoundStats() {
        this.roundStats = new PlayerStats();
    }

    @Override
    public boolean isShowRefreshButton() {
        return showRefreshButton;
    }

    @Override
    public void setShowRefreshButton(boolean showRefreshButton) {
        this.showRefreshButton = showRefreshButton;
    }

    @Override
    public long getExternalRoundId() {
        return externalRoundId;
    }

    @Override
    public void setExternalRoundId(long externalRoundId) {
        this.externalRoundId = externalRoundId;
    }

    @Override
    public long getRoundBuyInAmount() {
        return roundBuyInAmount;
    }

    @Override
    public void setRoundBuyInAmount(long roundBuyInAmount) {
        this.roundBuyInAmount = roundBuyInAmount;
    }

    @Override
    public IPlayerQuests getPlayerQuests() {
        return playerQuests;
    }

    @Override
    public void setPlayerQuests(IPlayerQuests playerQuests) {
        this.playerQuests = playerQuests;
    }

    @Override
    public IExperience getPrevXP() {
        return prevXP;
    }

    @Override
    public void setPrevXP(IExperience prevXP) {
        this.prevXP = prevXP;
    }

    @Override
    public void finishCurrentRound() {
        this.roundBuyInAmount = 0;
    }

    @Override
    public void makeBuyIn(long externalRoundId, long roundBuyInAmount) {
        this.externalRoundId = externalRoundId;
        this.roundBuyInAmount += roundBuyInAmount;
    }

    @Override
    public IActiveFrbSession getActiveFrbSession() {
        return activeFrbSession;
    }

    @Override
    public IActiveCashBonusSession getActiveCashBonusSession() {
        return activeCashBonusSession;
    }

    @Override
    public void setActiveCashBonusSession(IActiveCashBonusSession activeCashBonusSession) {
        this.activeCashBonusSession = activeCashBonusSession;
    }

    @Override
    public void setActiveFrbSession(IActiveFrbSession activeFrbSession) {
        this.activeFrbSession = activeFrbSession;
    }

    @Override
    public ITournamentSession getTournamentSession() {
        return tournamentSession;
    }

    @Override
    public void setTournamentSession(ITournamentSession tournamentSession) {
        this.tournamentSession = tournamentSession;
    }

    @Override
    public long getStake() {
        return stake;
    }

    @Override
    public void setStake(long stake) {
        this.stake = stake;
    }

    @Override
    public int getStakesReserve() {
        return stakesReserve;
    }

    @Override
    public void setStakesReserve(int stakesReserve) {
        this.stakesReserve = stakesReserve;
    }

    @Override
    public int getBuyInCount() {
        return buyInCount;
    }

    @Override
    public void setBuyInCount(int buyInCount) {
        this.buyInCount = buyInCount;
    }

    @Override
    public void incrementBuyInCount() {
        this.buyInCount++;
    }

    @Override
    public IPlayerBet createNewPlayerBet() {
        return new PlayerBet(getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoomPlayerInfo that = (AbstractRoomPlayerInfo) o;
        return id == that.id &&
                bankId == that.bankId &&
                gameSessionId == that.gameSessionId &&
                enterDate == that.enterDate &&
                wantSitOut == that.wantSitOut &&
                Objects.equals(nickname, that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bankId);
    }

    public String toShortString() {
        final StringBuilder sb = new StringBuilder("[");
        sb.append("accountId=").append(id);
        sb.append(", roomId=").append(roomId);
        sb.append(", seatNumber=").append(seatNumber);
        sb.append(", sessionId=").append(sessionId);
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", wantSitOut=").append(wantSitOut);
        sb.append(", externalRoundId=").append(externalRoundId);
        sb.append(", roundBuyInAmount=").append(roundBuyInAmount);
        sb.append(", buyInCount=").append(buyInCount);
        sb.append(", pendingOperation=").append(pendingOperation);
        sb.append(", stake=").append(stake);
        sb.append(']');
        return sb.toString();
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
        sb.append(", showRefreshButton=").append(showRefreshButton);
        sb.append(", externalRoundId=").append(externalRoundId);
        sb.append(", roundBuyInAmount=").append(roundBuyInAmount);
        sb.append(", pendingOperation=").append(pendingOperation);
        sb.append(", stats=").append(stats);
        sb.append(", roundStats=").append(roundStats);
        sb.append(", activeFrbSession=").append(activeFrbSession);
        sb.append(", activeCashBonusSession=").append(activeCashBonusSession);
        sb.append(", activeTournamentSession=").append(tournamentSession);
        sb.append(", playerQuests=").append(playerQuests);
        sb.append(", prevXP=").append(prevXP);
        sb.append(", stake=").append(stake);
        sb.append(", stakesReserve=").append(stakesReserve);
        sb.append(", lastOperationInfo='").append(lastOperationInfo).append("'");
        sb.append(", buyInCount=").append(buyInCount);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(roomId, true);
        output.writeInt(seatNumber, true);
        output.writeLong(bankId, true);
        output.writeString(sessionId);
        output.writeLong(gameSessionId, true);
        output.writeString(nickname);
        output.writeLong(enterDate, true);
        kryo.writeClassAndObject(output, currency);
        output.writeBoolean(wantSitOut);
        output.writeLong(externalRoundId, true);
        output.writeLong(roundBuyInAmount, true);
        kryo.writeClassAndObject(output, avatar);
        output.writeBoolean(pendingOperation);
        kryo.writeClassAndObject(output, stats);
        kryo.writeClassAndObject(output, roundStats);
        output.writeBoolean(showRefreshButton);
        kryo.writeClassAndObject(output, playerQuests);
        kryo.writeClassAndObject(output, prevXP);
        output.writeLong(stake, true);
        output.writeInt(stakesReserve, true);
        output.writeString(lastOperationInfo);
        output.writeInt(buyInCount, true);
        kryo.writeClassAndObject(output, activeFrbSession);
        kryo.writeClassAndObject(output, activeCashBonusSession);
        kryo.writeClassAndObject(output, tournamentSession);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        id = input.readLong(true);
        roomId = input.readLong(true);
        seatNumber = input.readInt(true);
        bankId = input.readLong(true);
        sessionId = input.readString();
        gameSessionId = input.readLong(true);
        nickname = input.readString();
        enterDate = input.readLong(true);
        currency = (ICurrency) kryo.readClassAndObject(input);
        wantSitOut = input.readBoolean();
        externalRoundId = input.readLong(true);
        roundBuyInAmount = input.readLong(true);
        avatar = (IAvatar) kryo.readClassAndObject(input);
        pendingOperation = input.readBoolean();
        stats = (IPlayerStats) kryo.readClassAndObject(input);
        roundStats = (IPlayerStats) kryo.readClassAndObject(input);
        showRefreshButton = input.readBoolean();
        playerQuests = (IPlayerQuests) kryo.readClassAndObject(input);
        prevXP = (IExperience) kryo.readClassAndObject(input);
        stake = input.readLong(true);
        stakesReserve = input.readInt(true);
        lastOperationInfo = input.readString();
        buyInCount = input.readInt(true);
        activeFrbSession = (IActiveFrbSession) kryo.readClassAndObject(input);
        activeCashBonusSession = (IActiveCashBonusSession) kryo.readClassAndObject(input);
        tournamentSession = (ITournamentSession) kryo.readClassAndObject(input);
    }

    protected abstract void serializeAdditional(JsonGenerator gen, SerializerProvider serializers) throws IOException;
    protected abstract void deserializeAdditional(JsonParser p, JsonNode node, DeserializationContext ctxt) throws IOException;

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

        gen.writeNumberField("id", id);
        gen.writeNumberField("roomId", roomId);
        gen.writeNumberField("seatNumber", seatNumber);
        gen.writeNumberField("bankId", bankId);
        gen.writeStringField("sessionId", sessionId);
        gen.writeNumberField("gameSessionId", gameSessionId);
        gen.writeStringField("nickname", nickname);
        gen.writeNumberField("enterDate", enterDate);
        gen.writeObjectField("currency", currency);
        gen.writeBooleanField("wantSitOut", wantSitOut);
        gen.writeNumberField("externalRoundId", externalRoundId);
        gen.writeNumberField("roundBuyInAmount", roundBuyInAmount);
        gen.writeObjectField("avatar", avatar);
        gen.writeBooleanField("pendingOperation", pendingOperation);
        gen.writeObjectField("stats", stats);
        gen.writeObjectField("roundStats", roundStats);
        gen.writeBooleanField("showRefreshButton", showRefreshButton);
        gen.writeObjectField("playerQuests", playerQuests);
        gen.writeObjectField("prevXP", prevXP);
        gen.writeObjectField("stake", stake);
        gen.writeNumberField("stakesReserve", stakesReserve);
        gen.writeStringField("lastOperationInfo", lastOperationInfo);
        gen.writeNumberField("buyInCount", buyInCount);
        gen.writeObjectField("activeFrbSession", activeFrbSession);
        gen.writeObjectField("activeCashBonusSession", activeCashBonusSession);
        gen.writeObjectField("tournamentSession", tournamentSession);

        serializeAdditional(gen, serializers);


    }

    @Override
    public RPI deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        id = node.get("id").longValue();
        roomId = node.get("roomId").longValue();
        seatNumber = node.get("seatNumber").intValue();
        bankId = node.get("bankId").intValue();
        sessionId = readNullableText(node, "sessionId");
        gameSessionId = node.get("gameSessionId").longValue();
        nickname = readNullableText(node, "nickname");
        enterDate = node.get("enterDate").asLong();
        currency = ((ObjectMapper) p.getCodec())
                .convertValue(node.get("currency"), ICurrency.class);
        wantSitOut = node.get("wantSitOut").asBoolean();
        externalRoundId = node.get("externalRoundId").asLong();
        roundBuyInAmount = node.get("roundBuyInAmount").asLong();
        avatar = ((ObjectMapper) p.getCodec())
                .convertValue(node.get("avatar"), IAvatar.class);
        pendingOperation = node.get("pendingOperation").asBoolean();
        stats = ((ObjectMapper) p.getCodec())
                .convertValue(node.get("stats"), IPlayerStats.class);
        roundStats = ((ObjectMapper) p.getCodec())
                .convertValue(node.get("roundStats"), IPlayerStats.class);
        showRefreshButton = node.get("showRefreshButton").asBoolean();
        playerQuests = ((ObjectMapper) p.getCodec())
                .convertValue(node.get("playerQuests"), IPlayerQuests.class);
        prevXP = ((ObjectMapper) p.getCodec())
                .convertValue(node.get("prevXP"), IExperience.class);
        stake = node.get("stake").asLong();
        stakesReserve = node.get("stakesReserve").asInt();
        lastOperationInfo = readNullableText(node, "lastOperationInfo");
        buyInCount = node.get("buyInCount").asInt();
        activeFrbSession = ((ObjectMapper) p.getCodec())
                .convertValue(node.get("activeFrbSession"), IActiveFrbSession.class);
        activeCashBonusSession = ((ObjectMapper) p.getCodec())
                .convertValue(node.get("activeCashBonusSession"), IActiveCashBonusSession.class);
        tournamentSession = ((ObjectMapper) p.getCodec())
                .convertValue(node.get("tournamentSession"), ITournamentSession.class);

        deserializeAdditional(p, node, ctxt);

        return getDeserialize();
    }

    protected abstract RPI getDeserialize();
}
