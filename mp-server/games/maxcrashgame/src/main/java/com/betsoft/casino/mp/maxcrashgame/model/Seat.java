package com.betsoft.casino.mp.maxcrashgame.model;

import com.betsoft.casino.mp.AbstractMultiNodeSeat;
import com.betsoft.casino.mp.common.AchievementHelper;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Seat extends AbstractMultiNodeSeat<Weapon, PlayerRoundInfo, ITreasure, ICrashGameRoomPlayerInfo, Seat> {
    private static final byte VERSION = 0;
    private static final Logger LOG = LogManager.getLogger(Seat.class);
    private Map<String, ICrashBetInfo> crashBets = new HashMap<>();
    private long canceledBetAmount;
    private long gameId;

    public Seat(ICrashGameRoomPlayerInfo playerInfo, IGameSocketClient socketClient, double currentRate, long gameId) {
        super(playerInfo, socketClient, currentRate);
        this.level = AchievementHelper.getPlayerLevel(playerInfo.getStats().getScore());
        this.gameId = gameId;
    }

    //only for tests/mocks
    public Seat() {
    }

    @Override
    public long getRoomId() {
        return super.getRoomId();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public Weapon createWeapon(int shots, SpecialWeaponType type) {
        return new Weapon(shots, type);
    }

    @Override
    public void initCurrentRoundInfo(ICrashGameRoomPlayerInfo playerInfo) {
        this.currentPlayerRoundInfo = new PlayerRoundInfo(playerInfo.getRoomId(), (int) gameId);
        super.initCurrentRoundInfo(playerInfo);
    }

    @Override
    public boolean isLevelUp() {
        return false;
    }

    @Override
    public PlayerRoundInfo getCurrentPlayerRoundInfo() {
        return currentPlayerRoundInfo;
    }

    @Override
    protected void readPlayerRoundInfo(byte version, Kryo kryo, Input input) {
        this.currentPlayerRoundInfo = (PlayerRoundInfo) kryo.readClassAndObject(input);
    }

    @Override
    protected void writeAdditionalFields(Kryo kryo, Output output) {
        super.writeAdditionalFields(kryo, output);
        output.writeByte(VERSION);
        kryo.writeObject(output, crashBets);
        output.writeLong(canceledBetAmount, true);
        output.writeLong(gameId, true);
    }

    @Override
    protected void readAdditionalFields(byte version, Kryo kryo, Input input) {
        super.readAdditionalFields(version, kryo, input);
        byte ver = input.readByte();
        //noinspection unchecked
        crashBets = kryo.readObject(input, HashMap.class);
        canceledBetAmount = input.readLong(true);
        gameId = input.readLong(true);
    }

    @Override
    protected void serializeAdditionalFields(JsonGenerator gen,
                                             SerializerProvider serializers) throws IOException {
        super.serializeAdditionalFields(gen, serializers);

        serializeMapField(gen, "crashBets", crashBets, new TypeReference<Map<String,ICrashBetInfo>>() {});
        gen.writeNumberField("canceledBetAmount", canceledBetAmount);
        gen.writeNumberField("gameId", gameId);
    }

    @Override
    protected void deserializeAdditionalFields(JsonParser p,
                                               JsonNode node,
                                               DeserializationContext ctxt) {
        super.deserializeAdditionalFields(p, node, ctxt);

        ObjectMapper om = (ObjectMapper) p.getCodec();

        crashBets = om.convertValue(node.get("crashBets"), new TypeReference<Map<String, ICrashBetInfo>>() {});
        canceledBetAmount = node.get("canceledBetAmount").longValue();
        gameId = node.get("gameId").longValue();
    }

    @Override
    protected void deserializePlayerRoundInfo(JsonParser p,
                                              JsonNode node,
                                              DeserializationContext ctxt) {
        ObjectMapper om = (ObjectMapper) p.getCodec();

        this.currentPlayerRoundInfo = om.convertValue(node.get("currentPlayerRoundInfo"), PlayerRoundInfo.class);
    }

    @Override
    protected Seat getDeserializer() {
        return this;
    }

    public Map<String, ICrashBetInfo> getCrashBets() {
        if (crashBets == null) {
            crashBets = new HashMap<>();
        }
        return Collections.unmodifiableMap(crashBets);
    }

    public void cancelCrashBet(String crashBetId) {
        ICrashBetInfo crashBet = getCrashBet(crashBetId);
        if (crashBet != null) {
            crashBets.remove(crashBetId);
            if (!crashBet.isReserved()) {
                incrementCanceledBetAmount(crashBet.getCrashBetAmount());
            }
        }
    }

    public boolean isAllCrashBetsEjected() {
        if (crashBets == null) {
            return true;
        }
        for (ICrashBetInfo crashBet : crashBets.values()) {
            if (!crashBet.isEjected()) {
                return false;
            }
        }
        return true;
    }

    public int getNotEjectedAutoPlayBetsCount() {
        int count = 0;
        if (crashBets != null) {
            for (ICrashBetInfo crashBet : crashBets.values()) {
                if (!crashBet.isEjected() && crashBet.isAutoPlay()) {
                    count++;
                }
            }
        }
        return count;
    }

    public void addCrashBet(String crashBetId, ICrashBetInfo crashBet) {
        if (crashBets.containsKey(crashBetId)) {
            throw new IllegalStateException("Duplicate crashBet with id=" + crashBetId);
        }
        crashBets.put(crashBetId, crashBet);
    }

    public ICrashBetInfo getCrashBet(String crashBetId) {
        return crashBets.get(crashBetId);
    }

    public void clearCrashBets() {
        crashBets.clear();
    }

    public int getCrashBetsCount() {
        return crashBets.size();
    }

    public long getCanceledBetAmount() {
        return canceledBetAmount;
    }

    public void setCanceledBetAmount(long canceledBetAmount) {
        this.canceledBetAmount = canceledBetAmount;
    }

    public void incrementCanceledBetAmount(long increment) {
        this.canceledBetAmount += increment;
    }

    public long retrieveCanceledBetAmount() {
        long result = canceledBetAmount;
        canceledBetAmount = 0;
        return result;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    @Override
    public String toString() {
        return "Seat [" + super.toString() + ", version=" + getActualVersion() + ",roomId=" + getRoomId() + ", gameId=" + gameId +
                ",lastActivityDate=" + getLastActivityDate() + ",crashBets=" + crashBets + ", canceledBetAmount=" + canceledBetAmount +
                ", activeServerId=" + activeServerId + ']';
    }

    public Money correctAmountWithCanceledBets(Money amount) {
        Money correctedBetAmount = amount;
        if (canceledBetAmount > 0) {
            long betAmountInCents = amount.toCents();
            LOG.debug("placeReservedBet: not zero canceledBetAmount, before correction canceledBetAmount={}, betAmountInCents={}",
                    canceledBetAmount, betAmountInCents);
            if (canceledBetAmount >= betAmountInCents) {
                correctedBetAmount = Money.ZERO;
                this.setCanceledBetAmount(canceledBetAmount - betAmountInCents);
            } else {
                correctedBetAmount = Money.fromCents(betAmountInCents - canceledBetAmount);
                this.setCanceledBetAmount(0);
            }
            LOG.debug("placeReservedBet: not zero canceledBetAmount, after correction seat.getCanceledBetAmount()={}, correctedBetAmount={}",
                    this.getCanceledBetAmount(), correctedBetAmount.toCents());
        }
        return correctedBetAmount;
    }

    public Map<String, ICrashBetInfo> getReservedCrashBets() {
        return crashBets.entrySet().stream()
                .filter(crashBet -> crashBet.getValue().isReserved())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void switchReservedFlag(){
        crashBets.forEach((key, crashBetInfo) -> crashBetInfo.setReserved(false));
    }
}
