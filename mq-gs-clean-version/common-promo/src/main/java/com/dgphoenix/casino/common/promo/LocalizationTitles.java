package com.dgphoenix.casino.common.promo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 7.12.2020.
 */
public class LocalizationTitles implements KryoSerializable {
    private static final byte VERSION = 0;
    private String tournamentRules;
    private String prizeAllocation;
    private String howToWin;

    public LocalizationTitles() {}

    public LocalizationTitles(String tournamentRules, String prizeAllocation, String howToWin) {
        this.tournamentRules = tournamentRules;
        this.prizeAllocation = prizeAllocation;
        this.howToWin = howToWin;
    }

    public String getTournamentRules() {
        return tournamentRules;
    }

    public void setTournamentRules(String tournamentRules) {
        this.tournamentRules = tournamentRules;
    }

    public String getPrizeAllocation() {
        return prizeAllocation;
    }

    public void setPrizeAllocation(String prizeAllocation) {
        this.prizeAllocation = prizeAllocation;
    }

    public String getHowToWin() {
        return howToWin;
    }

    public void setHowToWin(String howToWin) {
        this.howToWin = howToWin;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(tournamentRules);
        output.writeString(prizeAllocation);
        output.writeString(howToWin);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        tournamentRules = input.readString();
        prizeAllocation = input.readString();
        howToWin = input.readString();
    }
}
