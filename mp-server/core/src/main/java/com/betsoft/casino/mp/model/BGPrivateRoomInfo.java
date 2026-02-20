package com.betsoft.casino.mp.model;

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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

public class BGPrivateRoomInfo extends SingleNodeRoomInfo {
    private static final byte VERSION = 4;
    /** username of room owner*/
    private String ownerUsername;
    /** account id of room owner*/
    private long ownerAccountId;
    private String privateRoomId;
    private String joinUrl;
    private int countGamesPlayed;
    private long lastTimeActivity;
    /** flag for deactivation room*/
    private boolean isDeactivated;
    private long deactivationTime;
    /** account ids of kicked players */
    private Set<Long> kickedPlayers = new HashSet<>();

    public BGPrivateRoomInfo() {
        super();
    }

    public BGPrivateRoomInfo(long id, long templateId, long bankId, String name, GameType gameType, boolean closed, long updateDate, int gameServerId, long roundId, short maxSeats, short minSeats, MoneyType moneyType, RoomState state, int minBuyIn, Money stake, int mapId, String currency, int roundDuration) {
        super(id, templateId, bankId, name, gameType, closed, updateDate, gameServerId, roundId, maxSeats, minSeats, moneyType, state, minBuyIn, stake, mapId, currency, roundDuration);
    }

    /**
     * @param id room id
     * @param template IRoomTemplate template for creation
     * @param bankId bank id
     * @param updateDate date of creation
     * @param gameServerId server id
     * @param roundId room round id
     * @param state current state
     * @param mapId id of map
     * @param stake stake in room
     * @param currency currency in room
     */
    public BGPrivateRoomInfo(long id, IRoomTemplate template, long bankId, long updateDate, int gameServerId, long roundId, RoomState state, int mapId, Money stake, String currency) {
        super(id, template, bankId, updateDate, gameServerId, roundId, state, mapId, stake, currency);
    }

    @Override
    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    @Override
    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    @Override
    public String getJoinUrl() {
        return joinUrl;
    }

    public void setJoinUrl(String joinUrl) {
        this.joinUrl = joinUrl;
    }

    @Override
    public int getCountGamesPlayed() {
        return countGamesPlayed;
    }

    @Override
    public boolean isPrivateRoom() {
        return true;
    }

    public void setCountGamesPlayed(int countGamesPlayed) {
        this.countGamesPlayed = countGamesPlayed;
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

    @Override
    public void incrementCountGamesPlayed() {
        this.countGamesPlayed = this.countGamesPlayed + 1;
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
    public Set<Long> getKickedPlayers() {
        if (kickedPlayers == null) {
            kickedPlayers = new HashSet<>();
        }
        return kickedPlayers;
    }

    @Override
    public void setKickedPlayers(Set<Long> kickedPlayers) {
        this.kickedPlayers = kickedPlayers;
    }

    public void kickPlayer(long accountId) {
        kickedPlayers.add(accountId);
    }

    @Override
    public void cancelKick(long accountId) {
        kickedPlayers.remove(accountId);
    }

    @Override
    public boolean isPlayerKicked(long accountId) {
        return kickedPlayers.contains(accountId);
    }

    @Override
    public void clearKickedPlayers(){
        kickedPlayers.clear();
    }

    @Override
    public long getOwnerAccountId() {
        return ownerAccountId;
    }

    public void setOwnerAccountId(long ownerAccountId) {
        this.ownerAccountId = ownerAccountId;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeByte(VERSION);
        output.writeString(ownerUsername);
        output.writeString(privateRoomId);
        output.writeString(joinUrl);
        output.writeInt(countGamesPlayed, true);
        output.writeLong(lastTimeActivity, true);
        output.writeBoolean(isDeactivated);
        kryo.writeObject(output, getKickedPlayers());
        output.writeLong(ownerAccountId, true);
        output.writeLong(deactivationTime, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        byte version = input.readByte();
        ownerUsername = input.readString();
        privateRoomId = input.readString();
        joinUrl = input.readString();
        countGamesPlayed = input.readInt(true);
        lastTimeActivity = input.readLong(true);
        if (version > 0) {
            isDeactivated = input.readBoolean();
        }
        if (version > 1) {
            kickedPlayers = kryo.readObject(input, HashSet.class);;
        }
        if (version > 2) {
            ownerAccountId = input.readLong(true);
        }
        if (version > 3) {
            deactivationTime = input.readLong(true);
        }
    }

    @Override
    protected void serializeAdditional(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        super.serializeAdditional(gen, serializers);

        gen.writeStringField("ownerUsername", ownerUsername);
        gen.writeStringField("privateRoomId", privateRoomId);
        gen.writeStringField("joinUrl", joinUrl);
        gen.writeNumberField("countGamesPlayed", countGamesPlayed);
        gen.writeNumberField("lastTimeActivity", lastTimeActivity);
        gen.writeBooleanField("isDeactivated", isDeactivated);
        serializeSetField(gen, "kickedPlayers", getKickedPlayers(), new TypeReference<Set<Long>>() {});
        gen.writeNumberField("ownerAccountId", ownerAccountId);
        gen.writeNumberField("deactivationTime", deactivationTime);
    }

    @Override
    protected void deserializeAdditional(JsonParser p, JsonNode node, DeserializationContext ctxt)
            throws IOException {
        super.deserializeAdditional(p, node, ctxt);

        ownerUsername = readNullableText(node, "ownerUsername");
        privateRoomId = readNullableText(node, "privateRoomId");
        joinUrl = readNullableText(node, "joinUrl");
        countGamesPlayed = node.get("countGamesPlayed").asInt();
        lastTimeActivity = node.get("lastTimeActivity").asLong();
        isDeactivated = node.get("isDeactivated").asBoolean();
        kickedPlayers = ((ObjectMapper) p.getCodec())
                .convertValue(node.get("kickedPlayers"), new TypeReference<Set<Long>>() {});
        ownerAccountId = node.get("ownerAccountId").asLong();
        JsonNode deactivationTimeNode = node.get("deactivationTime");
        if(deactivationTimeNode == null || deactivationTimeNode.isNull()) {
            deactivationTime = 0L;
        } else {
            deactivationTime = deactivationTimeNode.asLong();
        }
    }

    @Override
    protected BGPrivateRoomInfo getDeserialize() {
        return this;
    }

    @Override
    public String toString() {
        return "BGPrivateRoomInfo [" + super.toString() +
                ", ownerUsername=" + ownerUsername +
                ", ownerAccountId=" + ownerAccountId +
                ", privateRoomId=" + privateRoomId +
                ", joinUrl=" + joinUrl +
                ", countGamesPlayed=" + countGamesPlayed +
                ", lastTimeActivity=" +  toHumanReadableFormat(lastTimeActivity, "yyyy-MM-dd HH:mm:ss.SSS") +
                ", kickedPlayers=" + kickedPlayers +
                ", isDeactivated=" + isDeactivated +
                ", deactivationTime=" + toHumanReadableFormat(deactivationTime, "yyyy-MM-dd HH:mm:ss.SSS") +
                ']';
    }
}
