package com.betsoft.casino.mp.model;

import java.io.IOException;

import com.betsoft.casino.mp.model.room.IMultiNodeRoomInfo;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

/**
 * User: flsh
 * Date: 09.11.17.
 */
public class MultiNodeRoomInfo extends AbstractRoomInfo<MultiNodeRoomInfo> implements IMultiNodeRoomInfo {
    private static final byte VERSION = 1;

    private long lastTimeActivity;
    /** flag for deactivation room*/
    private boolean isDeactivated;
    private long deactivationTime;

    public MultiNodeRoomInfo() {
        super();
    }

    public MultiNodeRoomInfo(long id, long templateId, long bankId, String name, GameType gameType, boolean closed,
                             long updateDate, long roundId, short maxSeats, short minSeats,
                             MoneyType moneyType, RoomState state, int minBuyIn, Money stake, int mapId, String currency,
                             int roundDuration) {
        super(id, templateId, bankId, name, gameType, closed, updateDate, roundId, maxSeats, minSeats,
                moneyType, state, minBuyIn, stake, mapId, currency, roundDuration);
    }

    /**
     * @param id room id
     * @param template IRoomTemplate template for creation
     * @param bankId bank id
     * @param updateDate date of creation
     * @param roundId room round id
     * @param state current state
     * @param mapId id of map
     * @param stake stake in room
     * @param currency currency in room
     */
    public MultiNodeRoomInfo(long id, IRoomTemplate template, long bankId, long updateDate, long roundId,
                             RoomState state, int mapId, Money stake, String currency) {
        super(id, template, bankId, updateDate, roundId, state, mapId, stake, currency);
    }

    @Override
    public long getLastTimeActivity() {
        return lastTimeActivity;
    }

    @Override
    public void setLastTimeActivity(long lastTimeActivity) {
        this.lastTimeActivity = lastTimeActivity;
    }

    @Override
    public void updateLastTimeActivity() {
        this.lastTimeActivity = System.currentTimeMillis();
    }

    public boolean isDeactivated() {
        return this.isDeactivated;
    }

    public void setDeactivated(boolean isDeactivated) {
        this.isDeactivated = isDeactivated;
        if(isDeactivated) {
            setDeactivationTime(System.currentTimeMillis());
        } else {
            setDeactivationTime(0);
        }
    }

    public long getDeactivationTime() {
        return deactivationTime;
    }

    public void setDeactivationTime(long deactivationTime) {
        this.deactivationTime = deactivationTime;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeByte(VERSION);
        output.writeLong(lastTimeActivity, true);
        output.writeBoolean(isDeactivated);
        output.writeLong(deactivationTime, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        byte version = input.readByte();
        if(version >= 1) {
            lastTimeActivity = input.readLong(true);
            isDeactivated = input.readBoolean();
            deactivationTime = input.readLong(true);
        }
    }

    @Override
    protected void serializeAdditional(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeNumberField("lastTimeActivity", lastTimeActivity);
        gen.writeBooleanField("isDeactivated", isDeactivated);
        gen.writeNumberField("deactivationTime", deactivationTime);
    }

    @Override
    protected void deserializeAdditional(JsonParser p, JsonNode node, DeserializationContext ctxt)
            throws IOException {
        JsonNode lastTimeActivityNode = node.get("lastTimeActivity");
        if(lastTimeActivityNode == null || lastTimeActivityNode.isNull()) {
            lastTimeActivity = System.currentTimeMillis();
        } else {
            lastTimeActivity = lastTimeActivityNode.asLong();
        }
        JsonNode isDeactivatedNode = node.get("isDeactivated");
        if(isDeactivatedNode == null || isDeactivatedNode.isNull()) {
            isDeactivated = false;
        } else {
            isDeactivated = isDeactivatedNode.asBoolean();
        }
        JsonNode deactivationTimeNode = node.get("deactivationTime");
        if(deactivationTimeNode == null || deactivationTimeNode.isNull()) {
            deactivationTime = 0L;
        } else {
            deactivationTime = deactivationTimeNode.asLong();
        }
    }

    @Override
    protected MultiNodeRoomInfo getDeserialize() {
        return this;
    }

    @Override
    public String toString() {
        return "MultiNodeRoomInfo [" + super.toString() +
                ", lastTimeActivity=" +  toHumanReadableFormat(lastTimeActivity, "yyyy-MM-dd HH:mm:ss.SSS") +
                ", isDeactivated=" + isDeactivated +
                ", deactivationTime=" + toHumanReadableFormat(deactivationTime, "yyyy-MM-dd HH:mm:ss.SSS") +
                ']';
    }
}
