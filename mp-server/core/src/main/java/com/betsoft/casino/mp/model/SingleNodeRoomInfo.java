package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.Positive;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

/**
 * User: flsh
 * Date: 09.11.17.
 */
public class SingleNodeRoomInfo extends AbstractRoomInfo<SingleNodeRoomInfo> implements ISingleNodeRoomInfo {
    private static final byte VERSION = 2;
    private static final Logger LOG = LogManager.getLogger(SingleNodeRoomInfo.class);

    @Positive
    protected int gameServerId;
    /** players who are waiting for open room */
    private Map<Long, Long> waitingOpenRoomPlayers;

    private long lastTimeActivity;
    /** flag for deactivation room*/
    private boolean isDeactivated;
    private long deactivationTime;

    public SingleNodeRoomInfo() {
        super();
    }

    public SingleNodeRoomInfo(long id, long templateId, long bankId, String name, GameType gameType, boolean closed,
                              long updateDate, int gameServerId, long roundId, short maxSeats, short minSeats,
                              MoneyType moneyType, RoomState state, int minBuyIn, Money stake, int mapId, String currency,
                              int roundDuration) {
        super(id, templateId, bankId, name, gameType, closed, updateDate, roundId, maxSeats, minSeats,
                moneyType, state, minBuyIn, stake, mapId, currency, roundDuration);
        this.gameServerId = gameServerId;
        waitingOpenRoomPlayers = new HashMap<>();
    }

    public SingleNodeRoomInfo(long id, IRoomTemplate template, long bankId, long updateDate, int gameServerId, long roundId,
                              RoomState state, int mapId, Money stake, String currency) {
        super(id, template, bankId, updateDate, roundId, state, mapId, stake, currency);
        this.gameServerId = gameServerId;
    }

    @Override
    public Map<Long, Long> getWaitingOpenRoomPlayersWithCheck() {
        if(waitingOpenRoomPlayers == null){
            waitingOpenRoomPlayers = new HashMap<>();
        }
        long currentTimeMillis = System.currentTimeMillis();
        waitingOpenRoomPlayers.entrySet().removeIf(entry -> {
            boolean isWaitingTimeExpired = currentTimeMillis > entry.getValue() + 30000;
            LOG.debug("getWaitingOpenRoomPlayersWithCheck: {}, {}",entry.getKey(), isWaitingTimeExpired);
            return isWaitingTimeExpired;
        });
        return waitingOpenRoomPlayers;
    }

    public void setWaitingOpenRoomPlayers(Map<Long, Long> waitingOpenRoomPlayers) {
        this.waitingOpenRoomPlayers = waitingOpenRoomPlayers;
    }

    @Override
    public void removePlayerFromWaitingOpenRoom(Long accountId) {
        getWaitingOpenRoomPlayersWithCheck().remove(accountId);
    }

    @Override
    public void addPlayerToWaitingOpenRoom(Long accountId) {
        getWaitingOpenRoomPlayersWithCheck().put(accountId, System.currentTimeMillis());
    }

    @Override
    public void setGameServerId(int gameServerId) {
        this.gameServerId = gameServerId;
    }

    @Override
    public int getGameServerId() {
        return gameServerId;
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
        output.writeInt(gameServerId, true);
        kryo.writeClassAndObject(output, getWaitingOpenRoomPlayersWithCheck());
        output.writeLong(lastTimeActivity, true);
        output.writeBoolean(isDeactivated);
        output.writeLong(deactivationTime, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        byte version = input.readByte();
        gameServerId = input.readInt(true);
        if(version >= 1){
            waitingOpenRoomPlayers = (Map<Long, Long>) kryo.readClassAndObject(input);
        }
        if(version >= 2) {
            lastTimeActivity = input.readLong(true);
            isDeactivated = input.readBoolean();
            deactivationTime = input.readLong(true);
        }
    }

    @Override
    protected void serializeAdditional(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeNumberField("gameServerId", gameServerId);
        serializeMapField(gen, "waitingOpenRoomPlayersWithCheck", getWaitingOpenRoomPlayersWithCheck(), new TypeReference<Map<Long,Long>>() {});
        gen.writeNumberField("lastTimeActivity", lastTimeActivity);
        gen.writeBooleanField("isDeactivated", isDeactivated);
        gen.writeNumberField("deactivationTime", deactivationTime);
    }

    @Override
    protected void deserializeAdditional(JsonParser p, JsonNode node, DeserializationContext ctxt)
            throws IOException {
        gameServerId = node.get("gameServerId").asInt();
        waitingOpenRoomPlayers = ((ObjectMapper) p.getCodec())
                .convertValue(node.get("waitingOpenRoomPlayersWithCheck"), new TypeReference<Map<Long, Long>>() {});
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
    protected SingleNodeRoomInfo getDeserialize() {
        return this;
    }

    @Override
    public String toString() {
        return "SingleNodeRoomInfo [" + super.toString() +
                ", gameServerId=" + gameServerId +
                ", waitingOpenRoomPlayers=" + getWaitingOpenRoomPlayersWithCheck() +
                ", lastTimeActivity=" +  toHumanReadableFormat(lastTimeActivity, "yyyy-MM-dd HH:mm:ss.SSS") +
                ", isDeactivated=" + isDeactivated +
                ", deactivationTime=" + toHumanReadableFormat(deactivationTime, "yyyy-MM-dd HH:mm:ss.SSS") +
                ']';
    }
}
