package com.dgphoenix.casino.common.promo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 22.11.16.
 */
public class GameBonusKey implements Serializable, KryoSerializable {
    private static final byte VERSION = 0;

    private long gameId;
    private String bonusName;

    public GameBonusKey() {
    }

    public GameBonusKey(long gameId, String bonusName) {
        this.gameId = gameId;
        this.bonusName = bonusName;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getBonusName() {
        return bonusName;
    }

    public void setBonusName(String bonusName) {
        this.bonusName = bonusName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameBonusKey that = (GameBonusKey) o;

        if (gameId != that.gameId) return false;
        return !(bonusName != null ? !bonusName.equals(that.bonusName) : that.bonusName != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (gameId ^ (gameId >>> 32));
        result = 31 * result + (bonusName != null ? bonusName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GameBonusKey[" +
                "gameId=" + gameId +
                ", bonusName='" + bonusName + '\'' +
                ']';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(gameId, true);
        output.writeString(bonusName);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        gameId = input.readLong(true);
        bonusName = input.readString();
    }
}
