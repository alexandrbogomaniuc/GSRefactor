package com.betsoft.casino.mp.maxblastchampions.model;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.maxblastchampions.model.math.EnemyRange;
import com.betsoft.casino.mp.maxblastchampions.model.math.EnemyType;
import com.betsoft.casino.mp.maxblastchampions.model.math.MathData;
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
    private static final byte VERSION = 3;
    protected long timeOfRoundStart;
    protected List<String> additionalBetData;
    protected String salt;
    protected double crashMult;
    protected long totalPot;
    protected long refundAmount;

    protected long totalPotWithoutRake;

    protected double rake;

    protected double kilometerMult;

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
        totalPot = 0;
        refundAmount = 0;
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
        if (totalBets.greaterThan(Money.ZERO) || totalPayouts.greaterThan(Money.ZERO) || !getAdditionalBetData().isEmpty()) {
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

        sb.append("bet").append(DELIMETER_VALUE_PARAM).append(totalBets.toDoubleCents()).append(DELIMETER_FIELDS);
        sb.append("totalPot").append(DELIMETER_VALUE_PARAM).append(totalPotWithoutRake).append(DELIMETER_FIELDS);
        sb.append("rake").append(DELIMETER_VALUE_PARAM).append(rake).append(DELIMETER_FIELDS);
        sb.append("payout").append(DELIMETER_VALUE_PARAM).append(totalPayouts.toDoubleCents()).append(DELIMETER_FIELDS);
        long crashKilometr = Math.round((crashMult - 1.00) * 100 * kilometerMult);
        sb.append("crashKilometr").append(DELIMETER_VALUE_PARAM).append(crashKilometr).append(DELIMETER_FIELDS);
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

    public long getTotalPot() {
        return totalPot;
    }

    public void setTotalPot(long totalPot) {
        this.totalPot = totalPot;
    }

    public long getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(long refundAmount) {
        this.refundAmount = refundAmount;
    }

    public long getTotalPotWithoutRake() {
        return totalPotWithoutRake;
    }

    public void setTotalPotWithoutRake(long totalPotWithoutRake) {
        this.totalPotWithoutRake = totalPotWithoutRake;
    }

    public double getRake() {
        return rake;
    }

    public void setRake(double rake) {
        this.rake = rake;
    }

    public double getKilometerMult() {
        return kilometerMult;
    }

    public void setKilometerMult(double kilometerMult) {
        this.kilometerMult = kilometerMult;
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

    void addCrashBetInfo(long accountId, long bet, long win, double mult){
        LOGGER.debug("addCrashBetInfo accountId: {}, bet: {}, win: {}, mult: {}", accountId, bet, win, mult);
        addTotalBets(Money.fromCents(bet));
        addTotalPayouts(Money.fromCents(win));
        getAdditionalBetData().add(bet + "|" + win + "|" + mult);
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
                ", totalPot=" + totalPot +
                ", refundAmount=" + refundAmount +
                ", totalPotWithoutRake=" + totalPotWithoutRake +
                ", rake=" + rake +
                ", kilometerMult=" + kilometerMult +
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
        output.writeLong(totalPot, true);
        output.writeLong(refundAmount, true);
        output.writeLong(totalPotWithoutRake, true);
        output.writeDouble(rake);
        output.writeDouble(kilometerMult);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        byte version = input.readByte();
        timeOfRoundStart = input.readLong(true);
        additionalBetData = (List<String>) kryo.readClassAndObject(input);
        salt = input.readString();
        crashMult = input.readDouble();
        if(version > 0) {
            totalPot = input.readLong(true);
            refundAmount = input.readLong(true);
        }
        if (version > 1) {
            totalPotWithoutRake = input.readLong(true);
            rake = input.readDouble();
        }
        if (version > 2) {
            kilometerMult = input.readDouble();
        }
    }

    protected void serializeAdditionalFields(JsonGenerator gen,
                                             SerializerProvider serializers) throws IOException {
        gen.writeNumberField("timeOfRoundStart", timeOfRoundStart);
        serializeListField(gen, "additionalBetData", getAdditionalBetData(), new TypeReference<List<String>>() {});
        gen.writeStringField("salt", salt);
        gen.writeNumberField("crashMult", crashMult);
        gen.writeNumberField("totalPot", totalPot);
        gen.writeNumberField("refundAmount", refundAmount);
        gen.writeNumberField("totalPotWithoutRake", totalPotWithoutRake);
        gen.writeNumberField("rake", rake);
        gen.writeNumberField("kilometerMult", kilometerMult);
    }

    protected void deserializeAdditionalFields(JsonParser p,
                                               JsonNode node,
                                               DeserializationContext ctxt) throws IOException {
        ObjectMapper om = (ObjectMapper) p.getCodec();

        timeOfRoundStart = node.get("timeOfRoundStart").longValue();
        additionalBetData = om.convertValue(node.get("additionalBetData"), new TypeReference<List<String>>() {});
        salt = node.get("salt").textValue();
        crashMult = node.get("crashMult").doubleValue();
        totalPot = node.get("totalPot").longValue();
        refundAmount = node.get("refundAmount").longValue();

        totalPotWithoutRake = node.get("totalPotWithoutRake").longValue();
        rake = node.get("rake").doubleValue();

        kilometerMult = node.get("kilometerMult").doubleValue();
    }

    @Override
    protected PlayerRoundInfo getDeserialized() {
        return this;
    }

}
