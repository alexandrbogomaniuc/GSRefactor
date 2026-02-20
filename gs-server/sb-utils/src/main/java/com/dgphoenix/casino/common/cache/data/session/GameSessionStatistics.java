package com.dgphoenix.casino.common.cache.data.session;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 26.01.18.
 */
public class GameSessionStatistics implements KryoSerializable {
    private static final byte VERSION = 0;
    private long gameSessionId;
    private long accountId;
    private Map<String, DeviationStatistics> sdStatistics = new HashMap<String, DeviationStatistics>();

    public GameSessionStatistics() {}

    public GameSessionStatistics(long gameSessionId, long accountId) {
        this.gameSessionId = gameSessionId;
        this.accountId = accountId;
    }

    public void update(String sdKey, int rounds, long income, long payout) {
        DeviationStatistics statistics = sdStatistics.get(sdKey);
        if(statistics == null) {
            sdStatistics.put(sdKey, new DeviationStatistics(rounds, income, payout));
        } else {
            statistics.increment(rounds, income, payout);
        }
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public Map<String, DeviationStatistics> getSdStatistics() {
        return sdStatistics;
    }

    public void setSdStatistics(
            Map<String, DeviationStatistics> sdStatistics) {
        this.sdStatistics = sdStatistics;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(gameSessionId, true);
        output.writeLong(accountId, true);
        kryo.writeClassAndObject(output, sdStatistics);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        final byte ver = input.readByte();
        gameSessionId = input.readLong(true);
        accountId = input.readLong(true);
        //noinspection unchecked
        sdStatistics = (Map) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameSessionStatistics [");
        sb.append("gameSessionId=").append(gameSessionId);
        sb.append(", accountId=").append(accountId);
        sb.append(", sdStatistics=").append(sdStatistics);
        sb.append(']');
        return sb.toString();
    }
}
