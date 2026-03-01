package com.abs.casino.common.cache.data.account;

import com.dgphoenix.casino.common.cache.CoinsCache;
import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.abs.casino.common.cache.data.account.IPlayerGameSettings;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * User: flsh
 * Date: 8/24/12
 */
public class PlayerGameSettings implements IDistributedCacheEntry, KryoSerializable, IPlayerGameSettings {
    private static final byte VERSION = 0;
    private static final String DELIMITER = ",";
    private long accountId;
    private int gameId;
    private Limit limit;
    private Integer defCoin;
    private String coins;

    public PlayerGameSettings() {

    }

    public PlayerGameSettings(long accountId, int gameId, List<Coin> coins, Limit limit, Integer defCoin) {
        this.accountId = accountId;
        this.gameId = gameId;
        this.limit = limit;
        this.defCoin = defCoin;
        this.coins = convert(coins);
    }

    private String convert(List<Coin> coins) {
        if (coins == null || coins.isEmpty()) {
            return null;
        }
        return StringUtils.toString(coins, DELIMITER, obj -> Long.toString(obj.getId()));
    }

    private List<Coin> convert(String coinsList) {
        if (StringUtils.isTrimmedEmpty(coinsList)) {
            return Collections.EMPTY_LIST;
        }
        List<Coin> result = new ArrayList<Coin>();
        StringTokenizer st = new StringTokenizer(coinsList, ",");
        while (st.hasMoreTokens()) {
            String value = st.nextToken();
            if (!StringUtils.isTrimmedEmpty(value)) {
                Coin coin = CoinsCache.getInstance().getCoin(Long.valueOf(value.trim()));
                if (coin != null && !result.contains(coin)) {
                    result.add(coin);
                }
            }
        }
        return result;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    @Override
    public List<Coin> getCoins() {
        return convert(coins);
    }

    public void setCoins(List<Coin> coins) {
        this.coins = convert(coins);
    }

    @Override
    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    @Override
    public Integer getDefCoin() {
        return defCoin;
    }

    public void setDefCoin(Integer defCoin) {
        this.defCoin = defCoin;
    }

    public boolean hasLimit() {
        return limit != null;
    }

    public boolean hasCoins() {
        return !StringUtils.isTrimmedEmpty(coins);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerGameSettings that = (PlayerGameSettings) o;

        if (gameId != that.gameId) return false;
        if (coins != null ? !coins.equals(that.coins) : that.coins != null) return false;
        if (defCoin != null ? !defCoin.equals(that.defCoin) : that.defCoin != null) return false;
        if (limit != null ? !limit.equals(that.limit) : that.limit != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (gameId ^ (gameId >>> 32));
        result = 31 * result + (limit != null ? limit.hashCode() : 0);
        result = 31 * result + (coins != null ? coins.hashCode() : 0);
        return result;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(accountId, true);
        output.writeInt(gameId, true);
        kryo.writeObjectOrNull(output, limit, Limit.class);
        kryo.writeObjectOrNull(output, defCoin, Integer.class);
        output.writeString(coins);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        accountId = input.readLong(true);
        gameId = input.readInt(true);
        limit = kryo.readObjectOrNull(input, Limit.class);
        defCoin = kryo.readObjectOrNull(input, Integer.class);
        coins = input.readString();
    }


    @Override
    public String toString() {
        return "PlayerGameSettings[" +
                "accountId=" + accountId +
                ", gameId=" + gameId +
                ", limit=" + limit +
                ", defCoin=" + defCoin +
                ", coins='" + coins + '\'' +
                ']';
    }
}
