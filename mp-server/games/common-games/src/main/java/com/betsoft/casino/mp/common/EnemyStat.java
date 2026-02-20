package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IEnemyStat;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.model.SpecialWeaponType;
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
import java.util.Objects;

public class EnemyStat implements IEnemyStat<WeaponStat, EnemyStat> {
    private static final byte VERSION = 2;

    private int cntShotsToEnemy;
    private Money payouts = Money.ZERO;
    private int cntKills;
    private Money hvBets = Money.ZERO;
    private Money mainBets = Money.ZERO;
    private Money turretBets = Money.ZERO;
    private boolean isHighValueEnemy;
    private Map<String, WeaponStat> specialWeaponsStats = new HashMap<>();
    private int betLevel = 1;
    private int cntTotalHitsToEnemy;
    private Map<Integer, Integer> chMultipliers = new HashMap<>();
    private Map<String, Double> payoutsFromItems = new HashMap<>();
    private Money killAwardWin = Money.ZERO;

    public EnemyStat() {
    }

    public EnemyStat(boolean isHighValueEnemy, int gameId) {
        this.isHighValueEnemy = isHighValueEnemy;
        cntTotalHitsToEnemy = 0;
        for (SpecialWeaponType weaponType : SpecialWeaponType.values()) {
            if (weaponType.getAvailableGameIds().contains(gameId))
                this.specialWeaponsStats.put(weaponType.getTitle(), new WeaponStat());
        }
    }

    public EnemyStat(boolean isHighValueEnemy, int gameId, int betLevel) {
        this.isHighValueEnemy = isHighValueEnemy;
        this.betLevel = betLevel;
        cntTotalHitsToEnemy = 0;
        for (SpecialWeaponType weaponType : SpecialWeaponType.values()) {
            if (weaponType.getAvailableGameIds().contains(gameId))
                this.specialWeaponsStats.put(weaponType.getTitle(), new WeaponStat());
        }
    }


    @Override
    public void updateData(Money stake, boolean isSpecial, String specialWeapon, Money payout, boolean isKilled,
                           Money betPayWeapon) {
        this.mainBets = this.mainBets.add(stake);
        this.cntTotalHitsToEnemy++;
        if (isSpecial) {
            WeaponStat weaponStat = this.specialWeaponsStats.get(specialWeapon);
            weaponStat.updateData(payout, betPayWeapon, isKilled);
        } else {
            this.turretBets = turretBets.add(stake);
            this.payouts = this.payouts.add(payout);
            this.cntShotsToEnemy++;
            if (isKilled)
                this.cntKills++;
        }
    }

    @Override
    public void updateData(Money stake, boolean isSpecial, String specialWeapon, Money payout, boolean isKilled, Money betPayWeapon,
                           int chMult, String specialItemName) {
        if (chMultipliers.isEmpty()) {
            chMultipliers.put(1, 0);
            chMultipliers.put(2, 0);
            chMultipliers.put(3, 0);
            chMultipliers.put(4, 0);
        }
        if (payoutsFromItems.isEmpty()) {
                payoutsFromItems.put("Money Wheel", 0.0);
                payoutsFromItems.put("Flash Blizzard", 0.0);
                payoutsFromItems.put("Enemy Seeker", 0.0);
                payoutsFromItems.put("Multiplier Bomb", 0.0);
                payoutsFromItems.put("Chain Reaction Shot", 0.0);
                payoutsFromItems.put("Arc Lighthing", 0.0);
                payoutsFromItems.put("Laser Net", 0.0);
        }
        this.mainBets = this.mainBets.add(stake);
        if (specialItemName == null) specialItemName = "";
        boolean isNeedUpdateShots = !specialItemName.equals("Flash Blizzard");
        this.cntTotalHitsToEnemy++;
        if (isSpecial) {
            WeaponStat weaponStat = this.specialWeaponsStats.get(specialWeapon);
            if (isNeedUpdateShots) {
                weaponStat.updateData(payout, betPayWeapon, isKilled);
            } else {
                Money payouts = weaponStat.getPayouts();
                Money bets = weaponStat.getPayBets();
                weaponStat.setPayouts(payouts.add(payout));
                weaponStat.setPayBets(bets.add(stake));
            }
            if (isKilled) {
                updatePayoutFromItemsAndChMults(payout, stake, specialItemName, chMult);
            }
        } else {
            this.turretBets = turretBets.add(stake);
            this.payouts = this.payouts.add(payout);
            if (isNeedUpdateShots) {
                this.cntShotsToEnemy++;
            }
            if (isKilled) {
                if (isNeedUpdateShots) {
                    this.cntKills++;
                }
                updatePayoutFromItemsAndChMults(payout, stake, specialItemName, chMult);
            }
        }
    }

    private void updatePayoutFromItemsAndChMults(Money payout, Money stake, String specialItemName, int chMult) {
        if (specialItemName != null && !specialItemName.equals("Flash Blizzard")) {
            int countChMults = chMultipliers.get(chMult);
            this.chMultipliers.put(chMult, ++countChMults);
        }
        if (!stake.greaterThan(Money.ZERO) && specialItemName != null && !specialItemName.isEmpty()) {
            Double old = payoutsFromItems.get(specialItemName);
            Double newPay = payout.toDoubleCents() + old;
            payoutsFromItems.put(specialItemName, newPay);
        }
    }

    public void updateKillAwardWinWithLevelUp(Money killAwardWin, boolean isSpecialWeapon, String specialWeapon) {
        if (isSpecialWeapon) {
            WeaponStat weaponStat = this.specialWeaponsStats.get(specialWeapon);
            Money currentPayouts = weaponStat.getPayouts().add(killAwardWin);
            weaponStat.setPayouts(currentPayouts);
        } else {
            this.payouts = this.payouts.add(killAwardWin);
        }
    }

    public void updateKillAwardWin(Money killAwardWin) {
        this.payouts = this.payouts.add(killAwardWin);
    }

    public void updatePayoutsFromItems(Money payout, String specialItemName) {
        if (payoutsFromItems.isEmpty()) {
            payoutsFromItems.put("Money Wheel", 0.0);
            payoutsFromItems.put("Flash Blizzard", 0.0);
            payoutsFromItems.put("Enemy Seeker", 0.0);
            payoutsFromItems.put("Multiplier Bomb", 0.0);
            payoutsFromItems.put("Chain Reaction Shot", 0.0);
            payoutsFromItems.put("Arc Lighthing", 0.0);
            payoutsFromItems.put("Laser Net", 0.0);
        }
        Double old = payoutsFromItems.get(specialItemName);
        Double newPay = payout.toDoubleCents() + old;
        payoutsFromItems.put(specialItemName, newPay);
    }

    public Money getKillAwardWin() {
        return payouts;
    }

    public Map<String, Double> getPayoutsFromItems() {
        if(payoutsFromItems == null){
            payoutsFromItems = new HashMap<>();
        }
        return payoutsFromItems;
    }

    public Map<Integer, Integer> getChMultipliers() {
        if(chMultipliers == null){
            chMultipliers = new HashMap<>();
        }
        return chMultipliers;
    }

    @Override

    public int getCntShotsToEnemy() {
        return cntShotsToEnemy;
    }

    @Override
    public Money getPayouts() {
        return payouts;
    }

    @Override
    public int getCntKills() {
        return cntKills;
    }

    @Override
    public Map<String, WeaponStat> getSpecialWeaponsStats() {
        return specialWeaponsStats;
    }

    @Override
    public void setCntShotsToEnemy(int cntShotsToEnemy) {
        this.cntShotsToEnemy = cntShotsToEnemy;
    }

    @Override
    public void setPayouts(Money payouts) {
        this.payouts = payouts;
    }

    @Override
    public void setCntKills(int cntKills) {
        this.cntKills = cntKills;
    }

    @Override
    public void setSpecialWeaponsStats(Map<String, WeaponStat> specialWeaponsStats) {
        this.specialWeaponsStats = specialWeaponsStats;
    }

    @Override
    public Money getHvBets() {
        return hvBets;
    }

    @Override
    public void setHvBets(Money hvBets) {
        this.hvBets = hvBets;
    }

    @Override
    public boolean isHighValueEnemy() {
        return isHighValueEnemy;
    }

    @Override
    public Money getTurretBets() {
        return turretBets;
    }

    @Override
    public void setMainBets(Money mainBets) {
        this.mainBets = mainBets;
    }

    @Override
    public Money getMainBets() {
        return mainBets;
    }

    @Override
    public void setHighValueEnemy(boolean highValueEnemy) {
        isHighValueEnemy = highValueEnemy;
    }

    public int getBetLevel() {
        return betLevel;
    }

    public void setBetLevel(int betLevel) {
        this.betLevel = betLevel;
    }

    public int getCntTotalHitsToEnemy() {
        return cntTotalHitsToEnemy;
    }

    public void setCntTotalHitsToEnemy(int cntTotalHitsToEnemy) {
        this.cntTotalHitsToEnemy = cntTotalHitsToEnemy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnemyStat enemyStat = (EnemyStat) o;
        return cntShotsToEnemy == enemyStat.cntShotsToEnemy &&
                cntKills == enemyStat.cntKills &&
                isHighValueEnemy == enemyStat.isHighValueEnemy &&
                Objects.equals(payouts, enemyStat.payouts) &&
                Objects.equals(hvBets, enemyStat.hvBets) &&
                Objects.equals(mainBets, enemyStat.mainBets) &&
                Objects.equals(turretBets, enemyStat.turretBets) &&
                Objects.equals(specialWeaponsStats, enemyStat.specialWeaponsStats) &&
                Objects.equals(chMultipliers, enemyStat.chMultipliers) &&
                Objects.equals(payoutsFromItems, enemyStat.payoutsFromItems) &&
                Objects.equals(killAwardWin, enemyStat.killAwardWin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cntShotsToEnemy, payouts, cntKills, hvBets, mainBets, turretBets, isHighValueEnemy, specialWeaponsStats, chMultipliers,
                payoutsFromItems, killAwardWin);
    }

    @Override
    public String toString() {
        return "EnemyStat[" +
                "cntShotsToEnemy=" + cntShotsToEnemy +
                ", payouts=" + payouts +
                ", cntKills=" + cntKills +
                ", hvBets=" + hvBets +
                ", mainBets=" + mainBets +
                ", turretBets=" + turretBets +
                ", isHighValueEnemy=" + isHighValueEnemy +
                ", specialWeaponsStats=" + specialWeaponsStats +
                ", betLevel=" + betLevel +
                ", chMultipliers=" + getChMultipliers() +
                ", payoutsFromMobs=" + getPayoutsFromItems() +
                ", killAwardWin=" + killAwardWin +
                ']';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(cntShotsToEnemy, true);
        kryo.writeObject(output, payouts);
        output.writeInt(cntKills, true);
        kryo.writeObject(output, hvBets);
        kryo.writeObject(output, mainBets);
        output.writeBoolean(isHighValueEnemy);
        kryo.writeClassAndObject(output, specialWeaponsStats);
        output.writeInt(betLevel, true);
        output.writeInt(cntTotalHitsToEnemy, true);
        kryo.writeObject(output, turretBets);
        kryo.writeClassAndObject(output, getChMultipliers());
        kryo.writeClassAndObject(output, getPayoutsFromItems());
        kryo.writeObject(output, killAwardWin);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        cntShotsToEnemy = input.readInt(true);
        payouts = kryo.readObject(input, Money.class);
        cntKills = input.readInt(true);
        hvBets = kryo.readObject(input, Money.class);
        mainBets = kryo.readObject(input, Money.class);
        isHighValueEnemy = input.readBoolean();
        //noinspection unchecked
        specialWeaponsStats = (Map<String, WeaponStat>) kryo.readClassAndObject(input);
        betLevel = input.readInt(true);
        cntTotalHitsToEnemy = input.readInt(true);
        if (version > 0) {
            turretBets = kryo.readObject(input, Money.class);
        }
        if (version > 1) {
            chMultipliers = (Map<Integer, Integer>) kryo.readClassAndObject(input);
            payoutsFromItems = (Map<String, Double>) kryo.readClassAndObject(input);
            killAwardWin = kryo.readObject(input, Money.class);
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("cntShotsToEnemy", cntShotsToEnemy);
        gen.writeObjectField("payouts", payouts);
        gen.writeNumberField("cntKills", cntKills);
        gen.writeObjectField("hvBets", hvBets);
        gen.writeObjectField("mainBets", mainBets);
        gen.writeBooleanField("isHighValueEnemy", isHighValueEnemy);
        serializeMapField(gen, "specialWeaponsStats", specialWeaponsStats, new TypeReference<Map<String, WeaponStat>>() {});
        gen.writeNumberField("betLevel", betLevel);
        gen.writeNumberField("cntTotalHitsToEnemy", cntTotalHitsToEnemy);
        gen.writeObjectField("turretBets", turretBets);
        serializeMapField(gen, "chMultipliers", getChMultipliers(), new TypeReference<Map<Integer, Integer>>() {});
        serializeMapField(gen, "payoutsFromItems", getPayoutsFromItems(), new TypeReference<Map<String,Double>>() {});
        gen.writeObjectField("killAwardWin", killAwardWin);
    }

    @Override
    public EnemyStat deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper om = (ObjectMapper) p.getCodec();

        cntShotsToEnemy = node.get("cntShotsToEnemy").intValue();
        payouts = om.convertValue(node.get("payouts"), Money.class);
        cntKills = node.get("cntKills").intValue();
        hvBets = om.convertValue(node.get("hvBets"), Money.class);
        mainBets = om.convertValue(node.get("mainBets"), Money.class);
        isHighValueEnemy = node.get("isHighValueEnemy").booleanValue();
        //noinspection unchecked
        specialWeaponsStats = om.convertValue(node.get("specialWeaponsStats"), new TypeReference<Map<String, WeaponStat>>() {});
        betLevel = node.get("betLevel").intValue();
        cntTotalHitsToEnemy = node.get("cntTotalHitsToEnemy").intValue();
        turretBets = om.convertValue(node.get("turretBets"), Money.class);

        chMultipliers = om.convertValue(node.get("chMultipliers"), new TypeReference<Map<Integer, Integer>>() {});
        payoutsFromItems = om.convertValue(node.get("chMultipliers"), new TypeReference<Map<String, Double>>() {});
        killAwardWin = om.convertValue(node.get("killAwardWin"), Money.class);

        return getDeserializer();
    }

    protected EnemyStat getDeserializer() {
        return this;
    }
}
