package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.service.IRoomPlayerInfoService;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import javax.validation.constraints.Positive;
import java.util.Map;
import java.util.Objects;

public class StubRoomInfo implements ISingleNodeRoomInfo, KryoSerializable {
    private static final byte VERSION = 0;
    private long id;
    private long templateId;
    private long bankId;
    private String name;
    private GameType gameType;
    private boolean closed;
    private long updateDate;
    @Positive
    private int gameServerId;
    @Positive
    private long roundId;
    @Positive
    private short maxSeats;
    @Positive
    private short minSeats;
    private MoneyType moneyType;
    private RoomState state;
    @Positive
    private int width;
    @Positive
    private int height;
    @Positive
    private int minBuyIn;
    private Money stake;
    private long lastRoundStartDate;
    @Positive
    private int mapId;
    private String currency;
    @Positive
    private int roundDuration;
    private boolean battlegroundMode;
    private long battlegroundBuyIn;
    private int battlegroundAmmoAmount;

    public StubRoomInfo() {}

    public StubRoomInfo(long id, long templateId, long bankId, String name, GameType gameType, boolean closed,
                        long updateDate, int gameServerId, long roundId, short maxSeats, short minSeats,
                        MoneyType moneyType, RoomState state, int minBuyIn, Money stake, int mapId,
                        String currency, int roundDuration) {
        this.id = id;
        this.templateId = templateId;
        this.bankId = bankId;
        this.name = name;
        this.gameType = gameType;
        this.closed = closed;
        this.updateDate = updateDate;
        this.gameServerId = gameServerId;
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
    }

    public StubRoomInfo(long id, IRoomTemplate template, long bankId, long updateDate, int gameServerId, long roundId,
                        RoomState state, int mapId, Money stake, String currency) {
        this.id = id;
        this.templateId = template.getId();
        this.bankId = bankId;
        this.name = template.getName() + " #" + id;
        this.gameType = template.getGameType();
        this.closed = false;
        this.updateDate = updateDate;
        this.gameServerId = gameServerId;
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

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
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

    @Override
    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
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

    @Override
    public long getUpdateDate() {
        return updateDate;
    }

    @Override
    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public int getGameServerId() {
        return gameServerId;
    }

    @Override
    public void setGameServerId(int gameServerId) {
        this.gameServerId = gameServerId;
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

    public void setMoneyType(MoneyType moneyType) {
        this.moneyType = moneyType;
    }

    @Override
    public boolean isBonusSession() {
        return moneyType.equals(MoneyType.CASHBONUS) ||
                moneyType.equals(MoneyType.FRB) || moneyType.equals(MoneyType.TOURNAMENT);
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
        return battlegroundBuyIn;
    }

    @Override
    public void setBattlegroundBuyIn(long battlegroundBuyIn) {
        this.battlegroundBuyIn = battlegroundBuyIn;
    }

    @Override
    public int getBattlegroundAmmoAmount() {
        return battlegroundAmmoAmount;
    }

    @Override
    public void setBattlegroundAmmoAmount(int battlegroundAmmoAmount) {
        this.battlegroundAmmoAmount = battlegroundAmmoAmount;
    }

    @Override
    public void removePlayerFromWaitingOpenRoom(Long accountId) {
    }

    @Override
    public void addPlayerToWaitingOpenRoom(Long accountId) {
    }

    @Override
    public Map<Long, Long> getWaitingOpenRoomPlayersWithCheck() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubRoomInfo roomInfo = (StubRoomInfo) o;
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
        output.writeInt(gameServerId, true);
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
        gameServerId = input.readInt(true);
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

    @Override
    public String toString() {
        return "RoomInfo [" + "id=" + id +
                ", templateId=" + templateId +
                ", bankId=" + bankId +
                ", name=" + name +
                ", gameType=" + gameType +
                ", closed=" + closed +
                ", updateDate=" + updateDate +
                ", gameServerId=" + gameServerId +
                ", roundId=" + roundId +
                ", maxSeats=" + maxSeats +
                ", minSeats=" + minSeats +
                ", moneyType=" + moneyType +
                ", state=" + state +
                ", width=" + width +
                ", height=" + height +
                ", minBuyIn=" + minBuyIn +
                ", stake=" + stake +
                ", lastRoundStartDate=" + lastRoundStartDate +
                ", mapId=" + mapId +
                ", currency=" + currency +
                ", roundDuration=" + roundDuration +
                ", battlegroundMode=" + battlegroundMode +
                ", battlegroundBuyIn=" + battlegroundBuyIn +
                ", battlegroundAmmoAmount=" + battlegroundAmmoAmount +
                ']';
    }
}
