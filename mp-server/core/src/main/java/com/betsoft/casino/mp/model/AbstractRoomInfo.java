package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.IRoomPlayerInfoService;
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

import javax.validation.constraints.Positive;

import java.io.IOException;
import java.util.Objects;
import static com.betsoft.casino.mp.service.AbstractRoomInfoService.DEFAULT_BATTLEGROUND_AMMO_AMOUNT;
import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

/**
 * User: flsh
 * Date: 24.12.2021.
 */
public abstract class AbstractRoomInfo<RI extends AbstractRoomInfo> implements IRoomInfo, 
        KryoSerializable, JsonSelfSerializable<RI> {
    private static final byte VERSION = 1;
    protected long id;
    protected long templateId;
    protected long bankId;
    protected String name;
    protected GameType gameType;
    protected boolean closed;
    protected long updateDate;
    @Positive
    protected long roundId;
    @Positive
    protected short maxSeats;
    @Positive
    protected short minSeats;
    protected MoneyType moneyType;
    protected RoomState state;
    @Positive
    protected int width;
    @Positive
    protected int height;
    @Positive
    protected int minBuyIn;
    protected Money stake;
    protected long lastRoundStartDate;
    @Positive
    protected int mapId;
    protected String currency;
    @Positive
    protected int roundDuration;
    protected boolean battlegroundMode;
    protected long battlegroundBuyIn;
    protected int battlegroundAmmoAmount;

    public AbstractRoomInfo() {}

    public AbstractRoomInfo(long id, long templateId, long bankId, String name, GameType gameType, boolean closed,
                    long updateDate, long roundId, short maxSeats, short minSeats,
                    MoneyType moneyType, RoomState state, int minBuyIn, Money stake, int mapId,
                    String currency, int roundDuration) {
        this.id = id;
        this.templateId = templateId;
        this.bankId = bankId;
        this.name = name;
        this.gameType = gameType;
        this.closed = closed;
        this.updateDate = updateDate;
        this.roundId = roundId;
        this.maxSeats = maxSeats;
        this.minSeats = minSeats;
        this.moneyType = moneyType;
        this.state = state;
        this.minBuyIn = minBuyIn;
        this.stake = stake;
        this.mapId = mapId;
        this.currency = currency;
        this.roundDuration = roundDuration;
        this.battlegroundMode = gameType.isBattleGroundGame();
    }

    public AbstractRoomInfo(long id, IRoomTemplate template, long bankId, long updateDate, long roundId,
                    RoomState state, int mapId, Money stake, String currency) {
        this.id = id;
        this.templateId = template.getId();
        this.bankId = bankId;
        this.name = template.getName() + " #" + id;
        this.gameType = template.getGameType();
        this.closed = false;
        this.updateDate = updateDate;
        this.roundId = roundId;
        this.maxSeats = template.getMaxSeats();
        this.minSeats = template.getMinSeats();
        this.moneyType = template.getMoneyType();
        this.state = state;
        this.minBuyIn = template.getMinBuyIn();
        this.stake = stake;
        this.mapId = mapId;
        this.currency = currency;
        this.roundDuration = template.getRoundDuration();
        this.battlegroundMode = gameType.isBattleGroundGame();
        if (gameType.isBattleGroundGame()) {
            this.battlegroundBuyIn = template.getBattlegroundBuyIn() == 0 ? stake.toCents() : template.getBattlegroundBuyIn();
            this.battlegroundAmmoAmount = template.getBattlegroundAmmoAmount() == 0 ?
                    DEFAULT_BATTLEGROUND_AMMO_AMOUNT : template.getBattlegroundAmmoAmount();
            if(!gameType.isCrashGame()){
                this.minSeats = 2;
                this.roundDuration = 90;
            }
        }
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(long templateId) {
        this.templateId = templateId;
    }

    @Override
    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    @Override
    public GameType getGameType() {
        return gameType;
    }

    @Override
    public void register(long serverId) {

    }

    @Override
    public void startNewRound(long roundId) {
        lastRoundStartDate = System.currentTimeMillis();
        this.roundId = roundId;
    }

    @Override
    public long getLastRoundStartDate() {
        return lastRoundStartDate;
    }

    public void setLastRoundStartDate(long lastRoundStartDate) {
        this.lastRoundStartDate = lastRoundStartDate;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {

    }

    @Override
    public void open() {

    }

    @Override
    public void unregister() {

    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    @Override
    public long getUpdateDate() {
        return updateDate;
    }

    @Override
    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    @Override
    public short getMaxSeats() {
        return maxSeats;
    }

    public void setMaxSeats(short maxSeats) {
        this.maxSeats = maxSeats;
    }

    @Override
    public short getMinSeats() {
        return minSeats;
    }

    public void setMinSeats(short minSeats) {
        this.minSeats = minSeats;
    }

    @Override
    public short getSeatsCount(IRoomPlayerInfoService roomPlayerInfoService) {
        return (short) roomPlayerInfoService.getForRoom(id).size();
    }

    @Override
    public MoneyType getMoneyType() {
        return moneyType;
    }

    @Override
    public boolean isBonusSession() {
        return moneyType.equals(MoneyType.CASHBONUS) ||
                moneyType.equals(MoneyType.FRB) || moneyType.equals(MoneyType.TOURNAMENT);
    }

    public void setMoneyType(MoneyType moneyType) {
        this.moneyType = moneyType;
    }

    @Override
    public RoomState getState() {
        return state;
    }

    @Override
    public void setState(RoomState state) {
        this.state = state;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public float getMinBuyIn() {
        return minBuyIn;
    }

    public void setMinBuyIn(int minBuyIn) {
        this.minBuyIn = minBuyIn;
    }

    @Override
    public Money getStake() {
        return stake;
    }

    public void setStake(Money stake) {
        this.stake = stake;
    }

    @Override
    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public int getRoundDuration() {
        return roundDuration;
    }

    @Override
    public void setRoundDuration(int roundDuration) {
        this.roundDuration = roundDuration;
    }

    @Override
    public boolean isBattlegroundMode() {
        return battlegroundMode;
    }

    @Override
    public void setBattlegroundMode(boolean battlegroundMode) {
        this.battlegroundMode = battlegroundMode;
    }

    @Override
    public long getBattlegroundBuyIn() {
        return gameType.isCrashGame() ? 0 : battlegroundBuyIn;
    }

    @Override
    public void setBattlegroundBuyIn(long battlegroundBuyIn) {
        this.battlegroundBuyIn = battlegroundBuyIn;
    }

    @Override
    public int getBattlegroundAmmoAmount() {
        return gameType.isCrashGame() ? 0 : battlegroundAmmoAmount;
    }

    @Override
    public void setBattlegroundAmmoAmount(int battlegroundAmmoAmount) {
        this.battlegroundAmmoAmount = battlegroundAmmoAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoomInfo roomInfo = (AbstractRoomInfo) o;
        return id == roomInfo.id &&
                templateId == roomInfo.templateId &&
                bankId == roomInfo.bankId &&
                gameType == roomInfo.gameType &&
                stake == roomInfo.stake &&
                currency.equals(roomInfo.currency) &&
                moneyType == roomInfo.moneyType &&
                battlegroundMode == roomInfo.battlegroundMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(templateId, true);
        output.writeLong(bankId, true);
        output.writeString(name);
        output.writeInt(gameType.ordinal(), true);
        output.writeBoolean(closed);
        output.writeLong(updateDate, true);
        output.writeLong(roundId, true);
        output.writeShort(maxSeats);
        output.writeShort(minSeats);
        output.writeInt(moneyType.ordinal(), true);
        output.writeInt(state.ordinal(), true);
        output.writeInt(width, true);
        output.writeInt(height, true);
        output.writeInt(minBuyIn, true);
        kryo.writeObject(output, stake);
        output.writeLong(lastRoundStartDate, true);
        output.writeInt(mapId, true);
        output.writeString(currency);
        output.writeInt(roundDuration, true);
        output.writeBoolean(battlegroundMode);
        output.writeLong(battlegroundBuyIn, true);
        output.writeInt(battlegroundAmmoAmount, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        id = input.readLong(true);
        templateId = input.readLong(true);
        bankId = input.readLong(true);
        name = input.readString();
        gameType = GameType.valueOf(input.readInt(true));
        closed = input.readBoolean();
        updateDate = input.readLong(true);
        roundId = input.readLong(true);
        maxSeats = input.readShort();
        minSeats = input.readShort();
        moneyType = MoneyType.valueOf(input.readInt(true));
        state = RoomState.valueOf(input.readInt(true));
        width = input.readInt(true);
        height = input.readInt(true);
        minBuyIn = input.readInt(true);
        stake = kryo.readObject(input, Money.class);
        lastRoundStartDate = input.readLong(true);
        mapId = input.readInt(true);
        currency = input.readString();
        roundDuration = input.readInt(true);
        battlegroundMode = input.readBoolean();
        battlegroundBuyIn = input.readLong(true);
        battlegroundAmmoAmount = input.readInt(true);
    }

    protected abstract void serializeAdditional(JsonGenerator gen, SerializerProvider serializers) throws IOException;
    protected abstract void deserializeAdditional(JsonParser p, JsonNode node, DeserializationContext ctxt) throws IOException;

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

        gen.writeNumberField("id", id);
        gen.writeNumberField("templateId", templateId);
        gen.writeNumberField("bankId", bankId);
        gen.writeStringField("name", name);
        gen.writeNumberField("gameTypeId", gameType.ordinal());
        gen.writeBooleanField("closed", closed);
        gen.writeNumberField("updateDate", updateDate);
        gen.writeNumberField("roundId", roundId);
        gen.writeNumberField("maxSeats", maxSeats);
        gen.writeNumberField("minSeats", minSeats);
        gen.writeNumberField("moneyTypeId", moneyType.ordinal());
        gen.writeNumberField("stateId", state.ordinal());
        gen.writeNumberField("width", width);
        gen.writeNumberField("height", height);
        gen.writeNumberField("minBuyIn", minBuyIn);
        gen.writeObjectField("stake", stake);
        gen.writeNumberField("lastRoundStartDate", lastRoundStartDate);
        gen.writeNumberField("mapId", mapId);
        gen.writeStringField("currency", currency);
        gen.writeNumberField("roundDuration", roundDuration);
        gen.writeBooleanField("battlegroundMode", battlegroundMode);
        gen.writeNumberField("battlegroundBuyIn", battlegroundBuyIn);
        gen.writeNumberField("battlegroundAmmoAmount", battlegroundAmmoAmount);
    
        serializeAdditional(gen, serializers);


    }

    @Override
    public RI deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        id = node.get("id").longValue();
        templateId = node.get("templateId").longValue();
        bankId = node.get("bankId").longValue();
        name = readNullableText(node, "name");
        gameType = GameType.valueOf(node.get("gameTypeId").asInt());
        closed = node.get("closed").asBoolean();
        updateDate = node.get("updateDate").longValue();
        roundId = node.get("roundId").longValue();
        maxSeats = node.get("maxSeats").shortValue();
        minSeats = node.get("minSeats").shortValue();
        moneyType = MoneyType.valueOf(node.get("moneyTypeId").asInt());
        state = RoomState.valueOf(node.get("stateId").asInt());
        width = node.get("width").asInt();
        height = node.get("height").asInt();
        minBuyIn = node.get("minBuyIn").asInt();
        stake = ((ObjectMapper) p.getCodec()).convertValue(node.get("stake"), Money.class);
        lastRoundStartDate = node.get("lastRoundStartDate").longValue();
        mapId = node.get("mapId").intValue();
        currency = readNullableText(node, "currency");
        roundDuration = node.get("roundDuration").asInt();
        battlegroundMode = node.get("battlegroundMode").booleanValue();
        battlegroundBuyIn = node.get("battlegroundBuyIn").longValue();
        battlegroundAmmoAmount = node.get("battlegroundAmmoAmount").intValue();

        deserializeAdditional(p, node, ctxt);

        return getDeserialize();
    }

    protected abstract RI getDeserialize();

    @Override
    public String toString() {
        return "id=" + id +
                ", templateId=" + templateId +
                ", bankId=" + bankId +
                ", name=" + name +
                ", gameType=" + gameType +
                ", closed=" + closed +
                ", updateDate=" + toHumanReadableFormat(updateDate, "yyyy-MM-dd HH:mm:ss.SSS") +
                ", roundId=" + roundId +
                ", maxSeats=" + maxSeats +
                ", minSeats=" + minSeats +
                ", moneyType=" + moneyType +
                ", state=" + state +
                ", width=" + width +
                ", height=" + height +
                ", minBuyIn=" + minBuyIn +
                ", stake=" + stake +
                ", lastRoundStartDate=" + toHumanReadableFormat(lastRoundStartDate, "yyyy-MM-dd HH:mm:ss.SSS") +
                ", mapId=" + mapId +
                ", currency=" + currency +
                ", roundDuration=" + roundDuration +
                ", battlegroundMode=" + battlegroundMode +
                ", battlegroundBuyIn=" + battlegroundBuyIn +
                ", battlegroundAmmoAmount=" + battlegroundAmmoAmount;
    }
}
