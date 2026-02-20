package com.betsoft.casino.mp.missionamazon.model;

import com.betsoft.casino.mp.common.AbstractActionSeat;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 21.09.17.
 */
public class Seat extends AbstractActionSeat<Weapon, PlayerRoundInfo, ITreasure, IActionRoomPlayerInfo, Seat> {
    private static final Logger LOG = LogManager.getLogger(Seat.class);
    private static final byte VERSION = 0;
    protected Map<Integer, Integer> seatGems = new HashMap<>();
    public Seat() {
        super();
    }

    public Seat(IActionRoomPlayerInfo playerInfo, IGameSocketClient socketClient, double currentRate) {
        super(playerInfo, socketClient, currentRate);
        Arrays.stream(SpecialWeaponType.values())
                .filter(weaponType -> weaponType.getAvailableGameIds().contains((int) GameType.MISSION_AMAZON.getGameId())
                        && !weaponType.isInternalServerShot())
                .forEach(weaponType -> weapons.put(weaponType, new Weapon(0, weaponType)));
        this.level = AchievementHelper.getPlayerLevel(playerInfo.getStats().getScore());
    }

    //need for disconnected seats
    public Seat(IActionRoomPlayerInfo playerInfo, Money stake) {
        super(playerInfo, stake);
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
    public void initCurrentRoundInfo(IActionRoomPlayerInfo playerInfo) {
        this.currentPlayerRoundInfo = new PlayerRoundInfo(playerInfo.getRoomId());
        super.initCurrentRoundInfo(playerInfo);
    }

    @Override
    public boolean isLevelUp() {
        int oldLevel = level;
        level = AchievementHelper.getPlayerLevel(playerInfo.getStats().getScore().getAmount() +
                playerInfo.getRoundStats().getScore().getAmount());
        return level > oldLevel;
    }

    @Override
    public PlayerRoundInfo getCurrentPlayerRoundInfo() {
        return currentPlayerRoundInfo;
    }

    @Override
    protected void readPlayerRoundInfo(byte version, Kryo kryo, Input input) {
        this.currentPlayerRoundInfo = (PlayerRoundInfo) kryo.readClassAndObject(input);
    }

    public Map<Integer, Integer> getSeatGems() {
        return seatGems == null ? new HashMap<>() : seatGems;
    }

    public void setSeatGems(Map<Integer, Integer> seatGems) {
        this.seatGems = seatGems;
    }

    public int addGem(Integer gemId, int numbers) {
        Map<Integer, Integer> seatGems = getSeatGems();
        Integer oldValue = seatGems.get(gemId);
        int newValue = oldValue == null ? numbers : oldValue + numbers;
        seatGems.put(gemId, newValue);
        return newValue;
    }

    public void clearGems() {
        getSeatGems().clear();
    }

    @Override
    protected void writeAdditionalFields(Kryo kryo, Output output) {
        super.writeAdditionalFields(kryo, output);
        output.writeByte(VERSION);
        kryo.writeObject(output, getSeatGems());
    }

    @Override
    protected void readAdditionalFields(byte version, Kryo kryo, Input input) {
        super.readAdditionalFields(version, kryo, input);
        byte ver = input.readByte();
        //noinspection unchecked
        seatGems = kryo.readObject(input, HashMap.class);
    }

    @Override
    protected void serializeAdditionalFields(JsonGenerator gen,
                                             SerializerProvider serializers) throws IOException {
        super.serializeAdditionalFields(gen, serializers);
        serializeMapField(gen, "seatGems", getSeatGems(), new TypeReference<Map<Integer,Integer>>() {});
    }

    @Override
    protected void deserializeAdditionalFields(JsonParser p,
                                               JsonNode node,
                                               DeserializationContext ctxt) {
        super.deserializeAdditionalFields(p, node, ctxt);

        ObjectMapper om = (ObjectMapper) p.getCodec();
        seatGems = om.convertValue(node.get("seatGems"), new TypeReference<HashMap<Integer, Integer>>() {});
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

    @Override
    public String toString() {
        return super.toString() + ", seatGems=" + getSeatGems();
    }
}
