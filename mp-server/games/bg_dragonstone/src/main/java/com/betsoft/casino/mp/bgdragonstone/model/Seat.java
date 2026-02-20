package com.betsoft.casino.mp.bgdragonstone.model;

import com.betsoft.casino.mp.common.AbstractBattlegroundSeat;
import com.betsoft.casino.mp.common.AchievementHelper;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.util.ObjectMap;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Seat extends AbstractBattlegroundSeat<Weapon, PlayerRoundInfo, ITreasure, IBattlegroundRoomPlayerInfo, Seat> {
    private static final Logger LOG = LogManager.getLogger(Seat.class);

    public Seat() {
        super();
    }

    public Seat(IBattlegroundRoomPlayerInfo playerInfo, IGameSocketClient socketClient, double currentRate) {
        super(playerInfo, socketClient, currentRate);
        for (SpecialWeaponType weaponType : SpecialWeaponType.values()) {
            if (weaponType.getAvailableGameIds().contains((int) GameType.BG_DRAGONSTONE.getGameId()) && !weaponType.isInternalServerShot()) {
                weapons.put(weaponType, new Weapon(0, weaponType));
            }
        }
        this.level = AchievementHelper.getPlayerLevel(playerInfo.getStats().getScore());
    }

    //need for disconnected seats
    public Seat(IBattlegroundRoomPlayerInfo playerInfo, Money stake) {
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
    public void initCurrentRoundInfo(IBattlegroundRoomPlayerInfo playerInfo) {
        this.currentPlayerRoundInfo = new PlayerRoundInfo(playerInfo.getRoomId(), true);
        super.initCurrentRoundInfo(playerInfo);
    }

    @Override
    public void transferWinToAmmo() {
        getLogger().debug("transferWinToAmmo for BG is not worked. do nothing");
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
}
