package com.betsoft.casino.mp.maxcrashgame.model;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.maxcrashgame.model.math.EnemyRange;
import com.betsoft.casino.mp.maxcrashgame.model.math.EnemyType;
import com.betsoft.casino.mp.maxcrashgame.model.math.MathData;
import com.betsoft.casino.mp.model.IEnemyType;
import com.betsoft.casino.mp.model.IPlayerBet;
import com.betsoft.casino.mp.model.Money;
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
import java.util.*;

public class PlayerRoundInfo extends AbstractPlayerRoundInfo<EnemyType, EnemyStat, PlayerRoundInfo> {
    private static final byte VERSION = 0;
    protected long timeOfRoundStart;
    protected List<String> additionalBetData;
    protected String salt;
    protected double crashMult;

    public PlayerRoundInfo() {
        super();
    }

    public PlayerRoundInfo(long roomId, int gameId) {
        this.gameId = gameId;
        this.roomId = roomId;
        this.statBoss = new BossStat(gameId);
        Set<Integer> possibleBetLevels = MathData.getPossibleBetLevels();
        for (IEnemyType enemyType : getEnemyTypes()) {
            for (Integer betLevel : possibleBetLevels) {
                String enemyNameKey = enemyType.getId() + "_" + enemyType.getName() + "_" + betLevel;
                this.statByEnemies.put(enemyNameKey, new EnemyStat(false, gameId, betLevel));
            }
        }
        realShotsCount = new HashMap<>();
        additionalWins = new HashMap<>();
        weaponStatBySources = new HashMap<>();
        moneyWheelCompleted = 0;
        moneyWheelPayouts = 0;
        totalDamage = 0;
        additionalBetData = new ArrayList<>();
        salt = "";
    }

    @Override
    public EnemyType[] getEnemyTypes() {
        return EnemyType.values();
    }

    @Override
    public EnemyType getEnemyTypeById(int id) {
        return EnemyType.getById(id);
    }

    @Override
    public List<EnemyType> getBaseEnemies() {
        return new ArrayList<>(EnemyRange.BASE_ENEMIES.getEnemies());
    }

    @Override
    public String getRTPStatData(int gameId) {
        return "";
    }

    @Override
    public IPlayerBet getPlayerBet(IPlayerBet newPlayerBet, int returnedBet) {
        if (totalBets.greaterThan(Money.ZERO) || totalPayouts.greaterThan(Money.ZERO) || !getAdditionalBetData().isEmpty() || returnedBet != 0) {
            newPlayerBet.setBet(totalBets.toDoubleCents());
            return super.getPlayerBet(newPlayerBet, returnedBet);
        } else {
            LOGGER.debug("getPlayerBet no activity, return empty data for VBA");
            newPlayerBet.setData("");
            return newPlayerBet;
        }
    }
    @Override
    protected String prepareVBAData(int returnedBet) {
        StringBuilder sb = new StringBuilder();
        sb.append("timeOfRoundEnd").append(DELIMETER_VALUE_PARAM).append(timeOfRoundEnd).append(DELIMETER_FIELDS);
        sb.append("playerRoundId").append(DELIMETER_VALUE_PARAM).append(playerRoundId).append(DELIMETER_FIELDS);
        sb.append("roomRoundId").append(DELIMETER_VALUE_PARAM).append(roomRoundId).append(DELIMETER_FIELDS);
        sb.append("roomId").append(DELIMETER_VALUE_PARAM).append(roomId).append(DELIMETER_FIELDS);
        sb.append("timeOfRoundStart").append(DELIMETER_VALUE_PARAM).append(timeOfRoundStart).append(DELIMETER_FIELDS);
        sb.append("crashMult").append(DELIMETER_VALUE_PARAM).append(crashMult).append(DELIMETER_FIELDS);
        StringBuilder sbBet = new StringBuilder();
        List<String> betData = getAdditionalBetData();
        for (String additionalBetDatum : betData) {
            sbBet.append(additionalBetDatum).append(DELIMETER_PARAM_FIELD);
        }

        sb.append("crashBetData").append(DELIMETER_VALUE_PARAM).append(sbBet);
        sb.append("saltDataRound").append(DELIMETER_VALUE_PARAM).append(getSalt());

        LOGGER.debug("crash prepareVBAData betData: {}, sb: {}", betData, sb);

        return sb.toString();
    }

    @Override
    protected void addSpecialWeapons(StringBuilder sb, Map<String, WeaponStat> weaponsStats) {
    }

    @Override
    public void addRoundInfo(AbstractPlayerRoundInfo<EnemyType, EnemyStat, PlayerRoundInfo> newRoundInfo) {
        super.addRoundInfo(newRoundInfo);
        newRoundInfo.getHitMissStatByWeapons().forEach((weaponId, aws) -> {
            Map<Integer, AdditionalWeaponStat> hitMissStatByWeapons = this.getHitMissStatByWeapons();
            AdditionalWeaponStat weaponStat = hitMissStatByWeapons.getOrDefault(weaponId, new AdditionalWeaponStat());
            weaponStat.addValues(aws.getNumberOfRealShots(), aws.getNumberOfHits(), aws.getNumberOfMiss(),
                    aws.getNumberOfKilledMiss(), aws.getNumberOfCompensateHits(), aws.getNumberOfMathHits());
            hitMissStatByWeapons.put(weaponId, weaponStat);
        });
    }

    @Override
    public void checkPay(Money checkPay) {
    }

    void addCrashBetInfo(long accountId, long bet, long win, double mult, Double autoEject){
        LOGGER.debug("addCrashBetInfo accountId: {}, bet: {}, win: {}, mult: {}, autoEject: {}", accountId, bet, win, mult, autoEject);
        addTotalBets(Money.fromCents(bet));
        addTotalPayouts(Money.fromCents(win));
        getAdditionalBetData().add(bet + "|" + win + "|" + mult + "|" + (autoEject != null ? String.valueOf(autoEject) : "0"));
    }

    @Override
    public String toString() {
        return "PlayerRoundInfo[" +
                "totalBets=" + totalBets +
                ", totalPayouts=" + totalPayouts +
                ", playerRoundId=" + playerRoundId +
                ", roomRoundId=" + roomRoundId +
                ", roomId=" + roomId +
                ", timeOfRoundEnd=" + timeOfRoundEnd +
                ", ammoAmountBuyIn=" + ammoAmountBuyIn +
                ", ammoAmountReturned=" + ammoAmountReturned +
                ", roomStake=" + roomStake +
                ", additionalData=" + additionalData +
                ", battlegroundMode=" + battlegroundMode +
                ", timeOfRoundStart=" + timeOfRoundStart +
                ", additionalBetData=" + getAdditionalBetData() +
                ", salt=" + salt +
                ", crashMult=" + crashMult +
                ']';
    }


    public long getTimeOfRoundStart() {
        return timeOfRoundStart;
    }

    public void setTimeOfRoundStart(long timeOfRoundStart) {
        this.timeOfRoundStart = timeOfRoundStart;
    }

    public List<String> getAdditionalBetData() {
        return additionalBetData == null ? new ArrayList<>() : additionalBetData;
    }

    public void setAdditionalBetData(List<String> additionalBetData) {
        this.additionalBetData = additionalBetData;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public double getCrashMult() {
        return crashMult;
    }

    public void setCrashMult(double crashMult) {
        this.crashMult = crashMult;
    }


    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeByte(VERSION);
        output.writeLong(timeOfRoundStart, true);
        kryo.writeClassAndObject(output, getAdditionalBetData());
        output.writeString(salt);
        output.writeDouble(crashMult);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        input.readByte();
        timeOfRoundStart = input.readLong(true);
        additionalBetData = (List<String>) kryo.readClassAndObject(input);
        salt = input.readString();
        crashMult = input.readDouble();
    }

    protected void serializeAdditionalFields(JsonGenerator gen,
                                             SerializerProvider serializers) throws IOException {
        gen.writeNumberField("timeOfRoundStart", timeOfRoundStart);
        serializeListField(gen, "additionalBetData", getAdditionalBetData(), new TypeReference<List<String>>() {});
        gen.writeStringField("salt", salt);
        gen.writeNumberField("crashMult", crashMult);
    }

    protected void deserializeAdditionalFields(JsonParser p,
                                               JsonNode node,
                                               DeserializationContext ctxt) throws IOException {
        ObjectMapper om = (ObjectMapper) p.getCodec();

        timeOfRoundStart = node.get("timeOfRoundStart").longValue();
        additionalBetData = om.convertValue(node.get("additionalBetData"), new TypeReference<List<String>>() {});
        salt = node.get("salt").textValue();
        crashMult = node.get("crashMult").doubleValue();
    }

    @Override
    protected PlayerRoundInfo getDeserialized() {
        return this;
    }

}
