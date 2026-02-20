package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BossStat implements KryoSerializable {
    private static final byte VERSION = 0;

    private Money betsExtra = Money.ZERO;
    private Money payoutsExtra = Money.ZERO;
    private Money payoutsMain = Money.ZERO;
    private int cntToBoss;
    private int cntKillsOfBoss;
    private Map<String, WeaponStat> specialWeaponsStats = new HashMap<>();

    public BossStat() {}

    public BossStat(int gameId) {
        for (SpecialWeaponType weaponType : SpecialWeaponType.values()) {
            if(weaponType.getAvailableGameIds().contains(gameId)) {
                this.specialWeaponsStats.put(weaponType.getTitle(), new WeaponStat());
            }
        }
    }

    public BossStat(Money betsExtra, Money payoutsExtra, Money payoutsMain, int cntToBoss, int cntKillsOfBoss,
                    Map<String, WeaponStat> specialWeaponsStats ) {
        this.betsExtra = betsExtra;
        this.payoutsExtra = payoutsExtra;
        this.payoutsMain = payoutsMain;
        this.cntToBoss = cntToBoss;
        this.cntKillsOfBoss = cntKillsOfBoss;
        this.specialWeaponsStats = specialWeaponsStats;
    }

    public Money getBetsExtra() {
        return betsExtra;
    }

    public Money getPayoutsExtra() {
        return payoutsExtra;
    }

    public Money getPayoutsMain() {
        return payoutsMain;
    }

    public int getCntToBoss() {
        return cntToBoss;
    }

    public int getCntKillsOfBoss() {
        return cntKillsOfBoss;
    }

    public void setCntToBoss(int cntToBoss) {
        this.cntToBoss = cntToBoss;
    }

    public void setCntKillsOfBoss(int cntKillsOfBoss) {
        this.cntKillsOfBoss = cntKillsOfBoss;
    }

    public void updateData(Money stake, Money mainBossPayout, boolean bossKilled,
                           boolean isSpecialWeapon, String specialWeapon, Money betPayWeapon) {
        cntToBoss++;
        this.betsExtra = this.betsExtra.add(stake);
        if(isSpecialWeapon) {
            WeaponStat weaponStat = this.specialWeaponsStats.get(specialWeapon);
            weaponStat.updateData(mainBossPayout, betPayWeapon, bossKilled);
        }else {
            this.payoutsMain = this.payoutsMain.add(mainBossPayout);
        }
        if (bossKilled)
            this.cntKillsOfBoss++;

    }

    public Map<String, WeaponStat> getSpecialWeaponsStats() {
        return specialWeaponsStats;
    }

    public void setSpecialWeaponsStats(Map<String, WeaponStat> specialWeaponsStats) {
        this.specialWeaponsStats = specialWeaponsStats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BossStat bossStat = (BossStat) o;
        return cntToBoss == bossStat.cntToBoss &&
                cntKillsOfBoss == bossStat.cntKillsOfBoss &&
                Objects.equals(betsExtra, bossStat.betsExtra) &&
                Objects.equals(payoutsExtra, bossStat.payoutsExtra) &&
                Objects.equals(payoutsMain, bossStat.payoutsMain) &&
                Objects.equals(specialWeaponsStats, bossStat.specialWeaponsStats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(betsExtra, payoutsExtra, payoutsMain, cntToBoss, cntKillsOfBoss, specialWeaponsStats);
    }

    public void setBetsExtra(Money betsExtra) {
        this.betsExtra = betsExtra;
    }

    public void setPayoutsExtra(Money payoutsExtra) {
        this.payoutsExtra = payoutsExtra;
    }

    public void setPayoutsMain(Money payoutsMain) {
        this.payoutsMain = payoutsMain;
    }

    @Override
    public String toString() {
        return "BossStat[" +
                "betsExtra=" + betsExtra +
                ", payoutsExtra=" + payoutsExtra +
                ", payoutsMain=" + payoutsMain +
                ", cntToBoss=" + cntToBoss +
                ", cntKillsOfBoss=" + cntKillsOfBoss +
                ", specialWeaponsStats=" + specialWeaponsStats +
                ']';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeObject(output, betsExtra);
        kryo.writeObject(output, payoutsExtra);
        kryo.writeObject(output, payoutsMain);
        output.writeInt(cntToBoss, true);
        output.writeInt(cntKillsOfBoss, true);
        kryo.writeClassAndObject(output, specialWeaponsStats);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        betsExtra = kryo.readObject(input, Money.class);
        payoutsExtra = kryo.readObject(input, Money.class);
        payoutsMain = kryo.readObject(input, Money.class);
        cntToBoss = input.readInt(true);
        cntKillsOfBoss = input.readInt(true);
        specialWeaponsStats = (Map<String, WeaponStat>) kryo.readClassAndObject(input);
    }
}
