package com.dgphoenix.casino.common.cache.data.bank;

import com.abs.casino.common.cache.data.bank.ICoin;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unnecessary type, can be simplified to int value
 */
public class Coin implements Serializable, ICoin<Coin> {
    private static final Logger LOG = LogManager.getLogger(Coin.class);
    private static final int VERSION = 0;
    private final long id;
    private final long value;

    private static final ConcurrentHashMap<Long, Coin> coinsById = new ConcurrentHashMap<>(128);
    private static final ConcurrentHashMap<Long, Coin> coinsByValue = new ConcurrentHashMap<>(128);

    static {
        addNewCoin(13, 1);
        addNewCoin(11, 2);
        addNewCoin(30, 3);
        addNewCoin(33, 4);
        addNewCoin(12, 5);
        addNewCoin(31, 6);
        addNewCoin(63, 7);
        addNewCoin(35, 8);
        addNewCoin(1, 10);
        addNewCoin(14, 15);
        addNewCoin(15, 20);
        addNewCoin(2, 25);
        addNewCoin(16, 30);
        addNewCoin(64, 35);
        addNewCoin(17, 40);
        addNewCoin(3, 50);
        addNewCoin(32, 60);
        addNewCoin(65, 65);
        addNewCoin(41, 70);
        addNewCoin(37, 75);
        addNewCoin(34, 80);
        addNewCoin(4, 100);
        addNewCoin(66, 125);
        addNewCoin(67, 130);
        addNewCoin(42, 140);
        addNewCoin(29, 150);
        addNewCoin(68, 175);
        addNewCoin(5, 200);
        addNewCoin(69, 220);
        addNewCoin(70, 225);
        addNewCoin(116, 240);
        addNewCoin(36, 250);
        addNewCoin(18, 300);
        addNewCoin(71, 320);
        addNewCoin(43, 350);
        addNewCoin(19, 400);
        addNewCoin(72, 450);
        addNewCoin(6, 500);
        addNewCoin(40, 600);
        addNewCoin(73, 650);
        addNewCoin(44, 700);
        addNewCoin(38, 750);
        addNewCoin(51, 800);
        addNewCoin(52, 900);
        addNewCoin(7, 1000);
        addNewCoin(92, 1200);
        addNewCoin(91, 1250);
        addNewCoin(74, 1300);
        addNewCoin(45, 1400);
        addNewCoin(8, 1500);
        addNewCoin(20, 2000);
        addNewCoin(9, 2500);
        addNewCoin(21, 3000);
        addNewCoin(46, 3500);
        addNewCoin(22, 4000);
        addNewCoin(10, 5000);
        addNewCoin(48, 6000);
        addNewCoin(47, 7000);
        addNewCoin(54, 7500);
        addNewCoin(93, 8000);
        addNewCoin(75, 9000);
        addNewCoin(23, 10000);
        addNewCoin(76, 12000);
        addNewCoin(49, 15000);
        addNewCoin(50, 20000);
        addNewCoin(53, 25000);
        addNewCoin(25, 30000);
        addNewCoin(26, 40000);
        addNewCoin(27, 50000);
        addNewCoin(94, 60000);
        addNewCoin(77, 75000);
        addNewCoin(28, 100000);
        addNewCoin(78, 150000);
        addNewCoin(55, 200000);
        addNewCoin(57, 250000);
        addNewCoin(79, 300000);
        addNewCoin(95, 400000);
        addNewCoin(58, 500000);
        addNewCoin(89, 600000);
        addNewCoin(80, 700000);
        addNewCoin(87, 750000);
        addNewCoin(56, 1000000);
        addNewCoin(81, 1400000);
        addNewCoin(88, 1500000);
        addNewCoin(59, 2000000);
        addNewCoin(60, 2500000);
        addNewCoin(90, 3000000);
        addNewCoin(61, 5000000);
        addNewCoin(62, 7500000);
        addNewCoin(82, 10000000);
        addNewCoin(104, 15000000);
        addNewCoin(83, 20000000);
        addNewCoin(105, 25000000);
        addNewCoin(84, 50000000);
        addNewCoin(85, 100000000);
        addNewCoin(86, 200000000);
        addNewCoin(96, 310);
        addNewCoin(97, 621);
        addNewCoin(98, 1242);
        addNewCoin(99, 3105);
        addNewCoin(100, 6210);
        addNewCoin(101, 12420);
        addNewCoin(102, 31049);
        addNewCoin(103, 62098);
        addNewCoin(106, 180);
        addNewCoin(107, 375);
        addNewCoin(108, 12);
        addNewCoin(109, 24);

        addNewCoin(110, 120);
        addNewCoin(111, 142);
        addNewCoin(112, 105);
        addNewCoin(113, 42);
        addNewCoin(114, 21);
        addNewCoin(115, 11);

        //Add new coin here
    }

    private Coin(long id, long value) {
        this.id = id;
        this.value = value;
    }

    @JsonCreator
    public static Coin fromParameters(@JsonProperty("id") long id,
                                      @JsonProperty("value") long value) {
        try {
            return getByValueWithCheckId(value, id);
        } catch (IllegalArgumentException e) {
            LOG.error("Can't get coin from cache, return new object", e);
            return new Coin(id, value);
        }
    }

    public static Coin getByValueWithCheckId(long value, long id) {
        Coin coin = getByValue(value);
        if (coin.getId() != id) {
            throw new IllegalArgumentException("Id=" + id + " != " + coin);
        }
        return coin;
    }

    private static Coin addNewCoin(long id, long value) {
        Coin coin = new Coin(id, value);
        Coin existed = coinsById.putIfAbsent(id, coin);
        if (existed != null) {
            throw new IllegalArgumentException("Coin with id=" + id + " already exists: " + existed);
        }
        Coin existedByValue = coinsByValue.putIfAbsent(value, coin);
        if (existedByValue != null) {
            throw new IllegalArgumentException("Coin with value=" + value + " already exists: " + existedByValue);
        }
        return coin;
    }

    public static Coin getById(long id) {
        Coin coin = coinsById.get(id);
        if (coin == null) {
            throw new IllegalArgumentException("Coin with id = " + id + " not exists");
        }
        return coin;
    }

    public static Coin getByValue(long value) {
        Coin coin = coinsByValue.get(value);
        if (coin == null) {
            throw new IllegalArgumentException("Coin with value = " + value + " not exists");
        }
        return coin;
    }

    public static List<Coin> getByValues(long... coinValues) {
        List<Coin> result = new ArrayList<Coin>();
        for (long coinValue : coinValues) {
            result.add(getByValue(coinValue));
        }
        return result;
    }

    public static List<Coin> getAll() {
        List<Coin> sortedList = new ArrayList<>(coinsById.values());
        Collections.sort(sortedList);
        return sortedList;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getValue() {
        return value;
    }

    @Override
    public Coin copy() {
        return new Coin(this.id, this.value);
    }

    public String toString() {
        return "Coin[id=" + id + ", value=" + value + "]";
    }

    @Override
    public int compareTo(Coin o) {
        return Long.compare(this.value, o.value);
    }

    public static List<Coin> copyCoins(List<Coin> coins) {
        return CollectionUtils.isEmpty(coins) ? null : new ArrayList<>(coins);
    }

    public static class CoinSerializer extends Serializer<Coin> {

        @Override
        public void write(Kryo kryo, Output output, Coin coin) {
            output.writeInt(VERSION);
            output.writeLong(coin.getId());
            output.writeLong(coin.getValue());
        }

        @Override
        public Coin read(Kryo kryo, Input input, Class<Coin> aClass) {
            int ver = input.readInt();
            long id = input.readLong();
            long value = input.readLong();
            return fromParameters(id, value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coin coin = (Coin) o;

        if (id != coin.id) return false;
        return value == coin.value;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ id >>> 32);
        result = 31 * result + (int) (value ^ value >>> 32);
        return result;
    }
}
