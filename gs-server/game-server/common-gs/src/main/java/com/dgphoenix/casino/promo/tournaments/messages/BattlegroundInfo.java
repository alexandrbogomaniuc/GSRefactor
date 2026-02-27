package com.abs.casino.promo.tournaments.messages;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.List;

public class BattlegroundInfo implements KryoSerializable, Serializable {
    private static final byte VERSION = 2;
    private long gameId;
    private String icon;
    private String rules;
    private List<Long> buyIns;
    private double rake;

    public BattlegroundInfo() {
    }

    public BattlegroundInfo(long gameId, String icon, String rules, List<Long> buyIns, double rake) {
        this.gameId = gameId;
        this.icon = icon;
        this.rules = rules;
        this.buyIns = buyIns;
        this.rake = rake;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public List<Long> getBuyIns() {
        return buyIns;
    }

    public void setBuyIns(List<Long> buyIns) {
        this.buyIns = buyIns;
    }

    public double getRake() {
        return rake;
    }

    public void setRake(double rake) {
        this.rake = rake;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(gameId, true);
        output.writeString(icon);
        output.writeString(rules);
        kryo.writeClassAndObject(output, buyIns);
        output.writeDouble(rake);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        gameId = input.readLong(true);
        icon = input.readString();
        rules = input.readString();
        buyIns = (List<Long>) kryo.readClassAndObject(input);
        if (version > 1) {
            rake = input.readDouble();
        }
    }

    @Override
    public String toString() {
        return "BattlegroundInfo{" +
                "gameId=" + gameId +
                ", icon='" + icon + '\'' +
                ", rules='" + rules + '\'' +
                ", buyIns=" + buyIns +
                ", rake=" + rake +
                '}';
    }
}
