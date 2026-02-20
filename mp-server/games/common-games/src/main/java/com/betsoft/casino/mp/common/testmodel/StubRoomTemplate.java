package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IRoomTemplate;
import com.betsoft.casino.mp.model.MoneyType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class StubRoomTemplate implements IRoomTemplate, KryoSerializable {
    public final static long DEFAULT_BANK_ID = -1;
    private static final byte VERSION = 0;
    private long id;
    //template with bankId=-1 is common template, if other is override
    private long bankId;
    private GameType gameType;
    private short maxSeats;
    private short minSeats;
    private MoneyType moneyType;
    private int width;
    private int height;
    private int minBuyIn;
    private int initialRooms;
    private int minFreeRooms;
    private int maxRooms;
    private String name;
    private int roundDuration;
    private boolean battlegroundMode;
    private long battlegroundBuyIn;
    private int battlegroundAmmoAmount;
    private boolean privateRoom;

    public StubRoomTemplate() {}

    public StubRoomTemplate(long id, long bankId, GameType gameType, short maxSeats, short minSeats, MoneyType moneyType,
                            int width, int height, int minBuyIn, int initialRooms, int minFreeRooms,
                            int maxRooms, String name, int roundDuration) {
        this.id = id;
        this.bankId = bankId;
        this.gameType = gameType;
        this.maxSeats = maxSeats;
        this.minSeats = minSeats;
        this.moneyType = moneyType;
        this.width = width;
        this.height = height;
        this.minBuyIn = minBuyIn;
        this.initialRooms = initialRooms;
        this.minFreeRooms = minFreeRooms;
        this.maxRooms = maxRooms;
        this.name = name;
        this.roundDuration = roundDuration;
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
    public long getBankId() {
        return bankId;
    }

    @Override
    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    @Override
    public int getInitialRooms() {
        return initialRooms;
    }

    @Override
    public void setInitialRooms(int initialRooms) {
        this.initialRooms = initialRooms;
    }

    @Override
    public int getMinFreeRooms() {
        return minFreeRooms;
    }

    @Override
    public void setMinFreeRooms(int minFreeRooms) {
        this.minFreeRooms = minFreeRooms;
    }

    @Override
    public GameType getGameType() {
        return gameType;
    }

    @Override
    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    @Override
    public short getMaxSeats() {
        return maxSeats;
    }

    @Override
    public void setMaxSeats(short maxSeats) {
        this.maxSeats = maxSeats;
    }

    @Override
    public short getMinSeats() {
        return minSeats;
    }

    @Override
    public void setMinSeats(short minSeats) {
        this.minSeats = minSeats;
    }

    @Override
    public MoneyType getMoneyType() {
        return moneyType;
    }

    @Override
    public void setMoneyType(MoneyType moneyType) {
        this.moneyType = moneyType;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getMinBuyIn() {
        return minBuyIn;
    }

    @Override
    public void setMinBuyIn(int minBuyIn) {
        this.minBuyIn = minBuyIn;
    }

    @Override
    public int getMaxRooms() {
        return maxRooms;
    }

    @Override
    public void setMaxRooms(int maxRooms) {
        this.maxRooms = maxRooms;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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
    public boolean isPrivateRoom() {
        return privateRoom;
    }

    @Override
    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubRoomTemplate that = (StubRoomTemplate) o;
        return id == that.id &&
                bankId == that.bankId &&
                minBuyIn == that.minBuyIn &&
                initialRooms == that.initialRooms &&
                minFreeRooms == that.minFreeRooms &&
                gameType == that.gameType &&
                moneyType == that.moneyType &&
                battlegroundMode == that.battlegroundMode &&
                privateRoom == that.privateRoom;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoomTemplate [");
        sb.append("id=").append(id);
        sb.append(", bankId=").append(bankId);
        sb.append(", gameType=").append(gameType);
        sb.append(", maxSeats=").append(maxSeats);
        sb.append(", minSeats=").append(minSeats);
        sb.append(", moneyType=").append(moneyType);
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        sb.append(", minBuyIn=").append(minBuyIn);
        sb.append(", initialRooms=").append(initialRooms);
        sb.append(", minFreeRooms=").append(minFreeRooms);
        sb.append(", maxRooms=").append(maxRooms);
        sb.append(", roundDuration=").append(roundDuration);
        sb.append(", battlegroundMode=").append(battlegroundMode);
        sb.append(", battlegroundBuyIn=").append(battlegroundBuyIn);
        sb.append(", battlegroundAmmoAmount=").append(battlegroundAmmoAmount);
        sb.append(", privateRoom=").append(privateRoom);
        sb.append(", name=").append(name);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(bankId);
        output.writeInt(gameType.ordinal(), true);
        output.writeShort(maxSeats);
        output.writeShort(minSeats);
        output.writeInt(moneyType.ordinal(), true);
        output.writeInt(width, true);
        output.writeInt(height, true);
        output.writeInt(minBuyIn, true);
        output.writeInt(initialRooms, true);
        output.writeInt(minFreeRooms, true);
        output.writeInt(maxRooms, true);
        output.writeString(name);
        output.writeInt(roundDuration, true);
        output.writeBoolean(battlegroundMode);
        output.writeLong(battlegroundBuyIn, true);
        output.writeInt(battlegroundAmmoAmount, true);
        output.writeBoolean(privateRoom);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        id = input.readLong(true);
        bankId = input.readLong();
        gameType = GameType.valueOf(input.readInt(true));
        maxSeats = input.readShort();
        minSeats = input.readShort();
        moneyType = MoneyType.valueOf(input.readInt(true));
        width = input.readInt(true);
        height = input.readInt(true);
        minBuyIn = input.readInt(true);
        initialRooms = input.readInt(true);
        minFreeRooms = input.readInt(true);
        maxRooms = input.readInt(true);
        name = input.readString();
        roundDuration = input.readInt(true);
        battlegroundMode = input.readBoolean();
        battlegroundBuyIn = input.readLong(true);
        battlegroundAmmoAmount = input.readInt(true);
        privateRoom = input.readBoolean();
    }
}
