package com.betsoft.casino.mp.common;

import java.io.IOException;

import com.betsoft.casino.mp.model.*;
import com.abs.casino.common.kpi.RoundKPIInfo;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * User: flsh
 * Date: 06.05.2022.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractActionGamePlayerRoundInfo<ENEMY_TYPE extends IEnemyType, ENEMY_STAT extends IEnemyStat, PRI extends IPlayerRoundInfo>
        extends AbstractPlayerRoundInfo<ENEMY_TYPE, ENEMY_STAT, PRI> implements IActionGamePlayerRoundInfo<ENEMY_TYPE, ENEMY_STAT, PRI> {
    private static final byte VERSION = 0;
    /** real shots count */
    protected int shotsCount;
    /** total bets of paid special weapons  */
    protected Money totalBetsSpecialWeapons = Money.ZERO;


    public AbstractActionGamePlayerRoundInfo() {
        super();
    }

    public AbstractActionGamePlayerRoundInfo(long roomId, int gameId) {
        super(roomId, gameId);
    }

    @Override
    public void updateStatNew(Money stake, boolean isBoss, boolean isSpecialWeapon,
                              String specialWeapon, Money payout, boolean isKilled,
                              String enemyKey, Money betPayWeapon) {
        shotsCount++;
        super.updateStatNew(stake, isBoss, isSpecialWeapon, specialWeapon, payout, isKilled, enemyKey, betPayWeapon);
    }

    @Override
    public void updateStatNewWithMultiplier(Money stake, boolean isBoss, boolean isSpecialWeapon,
                                            String specialWeapon, Money payout, boolean isKilled,
                                            String enemyKey, Money betPayWeapon, int chMult, String specialItemName) {
        shotsCount++;
        super.updateStatNewWithMultiplier(stake, isBoss, isSpecialWeapon, specialWeapon, payout, isKilled, enemyKey, betPayWeapon, chMult, specialItemName);
    }

    @Override
    public void updateStat(Money stake, boolean isBoss, Money extraBossPayout, Money mainBossPayout,
                           boolean isSpecialWeapon, String specialWeapon,
                           Money payout, boolean isKilled,
                           String enemy, Money betPayWeapon) {
        shotsCount++;
        super.updateStat(stake, isBoss, extraBossPayout, mainBossPayout, isSpecialWeapon, specialWeapon, payout, isKilled, enemy, betPayWeapon);
    }

    @Override
    public void addRoundInfo(AbstractPlayerRoundInfo<ENEMY_TYPE, ENEMY_STAT, PRI> newRoundInfo) {
        super.addRoundInfo(newRoundInfo);
        AbstractActionGamePlayerRoundInfo actionRoundInfo = (AbstractActionGamePlayerRoundInfo) newRoundInfo;
        this.addShotsCount(actionRoundInfo.getShotsCount());
        this.totalBetsSpecialWeapons = this.totalBetsSpecialWeapons.add(actionRoundInfo.getTotalBetsSpecialWeapons());
    }

    @Override
    public int getShotsCount() {
        return shotsCount;
    }

    @Override
    public void addShotsCount(long shotsCount) {
        this.shotsCount += shotsCount;
    }

    @Override
    public void setShotsCount(int shotsCount) {
        this.shotsCount = shotsCount;
    }

    @Override
    public Money getTotalBetsSpecialWeapons() {
        return totalBetsSpecialWeapons;
    }

    @Override
    public void setTotalBetsSpecialWeapons(Money totalBetsSpecialWeapons) {
        this.totalBetsSpecialWeapons = totalBetsSpecialWeapons;
    }

    @Override
    public void addTotalBetsSpecialWeapons(Money totalBetsSpecialWeapons) {
        this.totalBetsSpecialWeapons = this.totalBetsSpecialWeapons.add(totalBetsSpecialWeapons);
    }

    @Override
    public IPlayerBet getPlayerBet(IPlayerBet newPlayerBet, int returnedBet) {
        newPlayerBet.setBet(totalBets.toDoubleCents() + totalBetsSpecialWeapons.toDoubleCents());
        return super.getPlayerBet(newPlayerBet, returnedBet);
    }

    @Override
    protected void updateRoundKPIInfo(RoundKPIInfo roundKPIInfo) {
        super.updateRoundKPIInfo(roundKPIInfo);
        roundKPIInfo.setRealBet((long) (totalBets.toDoubleCents() + totalBetsSpecialWeapons.toDoubleCents()));
    }


    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeByte(VERSION);
        output.writeInt(shotsCount, true);
        kryo.writeObject(output, totalBetsSpecialWeapons);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        byte version = input.readByte();
        shotsCount = input.readInt(true);
        totalBetsSpecialWeapons = kryo.readObject(input, Money.class);
    }

    protected void serializeAdditionalFields(JsonGenerator gen,
                                                      SerializerProvider serializers) throws IOException {
        gen.writeNumberField("shotsCount", shotsCount);
        gen.writeObjectField("totalBetsSpecialWeapons", totalBetsSpecialWeapons);
    }

    protected void deserializeAdditionalFields(JsonParser p,
                                               JsonNode node,
                                               DeserializationContext ctxt) {
        ObjectMapper om = (ObjectMapper) p.getCodec();

        shotsCount = node.get("shotsCount").intValue();
        totalBetsSpecialWeapons = om.convertValue(node.get("totalBetsSpecialWeapons"), Money.class);
    }

}
