package com.betsoft.casino.mp.model.playerinfo;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.transport.MinePlace;
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
import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 29.04.2022.
 */
public abstract class AbstractActionRoomPlayerInfo extends AbstractRoomPlayerInfo implements IActionRoomPlayerInfo {
    private static final byte VERSION = 0;
    protected int specialWeaponId;
    protected Map<Integer, Integer> weapons = new HashMap<>();
    protected MaxQuestWeaponMode weaponMode;
    protected boolean allowWeaponSaveInAllGames;

    public AbstractActionRoomPlayerInfo() {}

    public AbstractActionRoomPlayerInfo(long id, long bankId, long roomId, int seatNumber, String sessionId, long gameSessionId, String nickname,
                                        IAvatar avatar, long enterDate, ICurrency currency, IPlayerStats stats, boolean showRefreshButton,
                                        Map<Integer, Integer> weapons, IPlayerQuests playerQuests, long stake, int stakesReserve,
                                        MaxQuestWeaponMode weaponMode, boolean allowWeaponSaveInAllGames) {
        super(id, bankId, roomId, seatNumber, sessionId, gameSessionId, nickname, avatar, enterDate, currency, stats, showRefreshButton,
                playerQuests, stake, stakesReserve);
        this.specialWeaponId = -1;
        if (weapons != null) {
            this.weapons = weapons;
        }
        this.weaponMode = weaponMode;
        this.allowWeaponSaveInAllGames = allowWeaponSaveInAllGames;
    }

    @Override
    public int getSpecialWeaponId() {
        return specialWeaponId;
    }

    @Override
    public void setSpecialWeaponId(int specialWeaponId) {
        this.specialWeaponId = specialWeaponId;
    }

    @Override
    public boolean isAllowWeaponSaveInAllGames() {
        return allowWeaponSaveInAllGames;
    }

    @Override
    public void setAllowWeaponSaveInAllGames(boolean allowWeaponSaveInAllGames) {
        this.allowWeaponSaveInAllGames = allowWeaponSaveInAllGames;
    }

    @Override
    public Map<Integer, Integer> getWeapons() {
        return weapons;
    }

    @Override
    public void setWeapons(Map<Integer, Integer> weapons) {
        if (weapons == null) {
            this.weapons.clear();
        } else {
            if (this.weapons == null) {
                this.weapons = new HashMap<>();
            } else {
                this.weapons.clear();
            }
            this.weapons.putAll(weapons);
        }
    }

    @Override
    public MaxQuestWeaponMode getWeaponMode() {
        return weaponMode == null ? MaxQuestWeaponMode.LOOT_BOX : weaponMode;
    }

    @Override
    public void setWeaponMode(MaxQuestWeaponMode weaponMode) {
        this.weaponMode = weaponMode;
    }

    @Override
    public IFreeShots createNewFreeShots() {
        return new FreeShots();
    }

    @Override
    public IMinePlace getNewMinePlace(long date, int rid, int seatId, float x, float y, String mineId) {
        return new MinePlace(date, rid, seatId, x, y, mineId);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(specialWeaponId, true);
        kryo.writeClassAndObject(output, weapons);
        output.writeInt(getWeaponMode().ordinal(), true);
        output.writeBoolean(allowWeaponSaveInAllGames);
        super.write(kryo, output);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        specialWeaponId = input.readInt(true);
        weapons = (Map<Integer, Integer>) kryo.readClassAndObject(input);
        weaponMode = MaxQuestWeaponMode.valueOf(input.readInt(true));
        allowWeaponSaveInAllGames = input.readBoolean();
        super.read(kryo, input);
    }

    protected void serializeAdditional(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("specialWeaponId", specialWeaponId);
        serializeMapField(gen, "weapons", weapons, new TypeReference<Map<Integer,Integer>>() {});
        gen.writeNumberField("weaponModeId", getWeaponMode().ordinal());
        gen.writeBooleanField("allowWeaponSaveInAllGames", allowWeaponSaveInAllGames);
    }

    protected void deserializeAdditional(JsonParser p, JsonNode node, DeserializationContext ctxt) throws IOException {
        specialWeaponId = node.get("specialWeaponId").asInt();
        weapons = ((ObjectMapper) p.getCodec()).convertValue(node.get("weapons"), new TypeReference<Map<Integer, Integer>>() {});
        weaponMode = MaxQuestWeaponMode.valueOf(node.get("weaponModeId").intValue());
        allowWeaponSaveInAllGames = node.get("allowWeaponSaveInAllGames").asBoolean();
    }

}
