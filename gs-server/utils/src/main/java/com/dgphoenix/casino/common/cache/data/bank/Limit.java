package com.abs.casino.common.cache.data.bank;

import com.abs.casino.common.cache.data.bank.ILimit;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Limit implements Serializable, ILimit<Limit> {
    private static final int VERSION = 0;
    private static final Logger LOG = LogManager.getLogger(Limit.class);
    private static final long GENERIC_LIMIT_ID = -1;
    private final long id;
    private final int minValue;
    private final int maxValue;

    private static final ConcurrentHashMap<Pair<Integer, Integer>, Limit> limitsByValue = new ConcurrentHashMap<>(256);
    //Contains only registered ids (> 0)
    private static final ConcurrentHashMap<Long, Limit> limitsById = new ConcurrentHashMap<>(256);

    static {
        addLimitWithRegisteredId(24, 2, 25);
        addLimitWithRegisteredId(22, 5, 10000);
        addLimitWithRegisteredId(28, 10, 10000); // mg
        addLimitWithRegisteredId(13, 25, 500);
        addLimitWithRegisteredId(12, 25, 2500);
        addLimitWithRegisteredId(11, 25, 12500);
        addLimitWithRegisteredId(20, 50, 5000);
        addLimitWithRegisteredId(29, 100, 100);
        addLimitWithRegisteredId(17, 100, 1000);
        addLimitWithRegisteredId(74, 100, 5000);
        addLimitWithRegisteredId(18, 100, 7500);
        addLimitWithRegisteredId(1, 100, 10000);
        addLimitWithRegisteredId(19, 100, 20000);
        addLimitWithRegisteredId(15, 100, 25000);
        addLimitWithRegisteredId(8, 100, 30000);
        addLimitWithRegisteredId(10, 100, 50000);
        addLimitWithRegisteredId(14, 100, 100000);
        addLimitWithRegisteredId(50, 100, 150000);
        addLimitWithRegisteredId(27, 100, 200000);
        addLimitWithRegisteredId(51, 100, 250000);
        addLimitWithRegisteredId(52, 100, 600000);
        addLimitWithRegisteredId(53, 100, 1500000);
        addLimitWithRegisteredId(54, 100, 2000000);
        addLimitWithRegisteredId(55, 100, 4000000);
        addLimitWithRegisteredId(56, 100, 6000000);
        addLimitWithRegisteredId(30, 200, 20000);
        addLimitWithRegisteredId(21, 300, 9900);
        addLimitWithRegisteredId(85, 300, 10000);
        addLimitWithRegisteredId(75, 300, 15000);
        addLimitWithRegisteredId(16, 300, 24900);
        addLimitWithRegisteredId(23, 300, 30000);
        addLimitWithRegisteredId(9, 300, 50000);
        addLimitWithRegisteredId(57, 300, 150000);
        addLimitWithRegisteredId(58, 300, 200000);
        addLimitWithRegisteredId(59, 300, 250000);
        addLimitWithRegisteredId(60, 300, 600000);
        addLimitWithRegisteredId(61, 300, 1500000);
        addLimitWithRegisteredId(62, 300, 2000000);
        addLimitWithRegisteredId(63, 300, 4000000);
        addLimitWithRegisteredId(64, 300, 6000000);
        addLimitWithRegisteredId(87, 500, 10000);
        addLimitWithRegisteredId(6, 500, 20000);
        addLimitWithRegisteredId(31, 500, 25000);
        addLimitWithRegisteredId(7, 500, 30000);
        addLimitWithRegisteredId(2, 500, 50000);
        addLimitWithRegisteredId(77, 500, 500000);
        addLimitWithRegisteredId(32, 1000, 50000);
        addLimitWithRegisteredId(86, 1000, 10000);
        addLimitWithRegisteredId(3, 1000, 100000);
        addLimitWithRegisteredId(65, 1000, 150000);
        addLimitWithRegisteredId(66, 1000, 200000);
        addLimitWithRegisteredId(67, 1000, 250000);
        addLimitWithRegisteredId(78, 1000, 500000);
        addLimitWithRegisteredId(68, 1000, 600000);
        addLimitWithRegisteredId(69, 1000, 1500000);
        addLimitWithRegisteredId(70, 1000, 2000000);
        addLimitWithRegisteredId(79, 1000, 2500000);
        addLimitWithRegisteredId(71, 1000, 4000000);
        addLimitWithRegisteredId(72, 1000, 6000000);
        addLimitWithRegisteredId(25, 1900, 190000);
        addLimitWithRegisteredId(33, 2000, 100000);
        addLimitWithRegisteredId(84, 2000, 1500000);
        addLimitWithRegisteredId(26, 2000, 200000);
        addLimitWithRegisteredId(49, 2500, 50000);
        addLimitWithRegisteredId(34, 2500, 200000);
        addLimitWithRegisteredId(76, 3000, 500000);
        addLimitWithRegisteredId(35, 5000, 250000);
        addLimitWithRegisteredId(73, 5000, 300000);
        addLimitWithRegisteredId(4, 5000, 500000);
        addLimitWithRegisteredId(36, 10000, 500000);
        addLimitWithRegisteredId(5, 10000, 1000000);
        addLimitWithRegisteredId(37, 20000, 1000000);
        addLimitWithRegisteredId(38, 25000, 2000000);
        addLimitWithRegisteredId(39, 50000, 2500000);
        addLimitWithRegisteredId(40, 100000, 5000000);
        addLimitWithRegisteredId(41, 200000, 10000000);
        addLimitWithRegisteredId(42, 250000, 20000000);
        addLimitWithRegisteredId(43, 500000, 25000000);
        addLimitWithRegisteredId(80, 500000, 500000000);
        addLimitWithRegisteredId(44, 1000000, 50000000);
        addLimitWithRegisteredId(81, 1000000, 500000000);
        addLimitWithRegisteredId(82, 1000000, 1500000000);
        addLimitWithRegisteredId(45, 2000000, 100000000);
        addLimitWithRegisteredId(83, 2000000, 1500000000);
        addLimitWithRegisteredId(46, 2500000, 250000000);
        addLimitWithRegisteredId(47, 5000000, 500000000);
        addLimitWithRegisteredId(48, 10000000, 1000000000);
        addLimitWithRegisteredId(88, 500, 300000);
        addLimitWithRegisteredId(89, 500, 60000);
        addLimitWithRegisteredId(90, 5000, 600000);
        addLimitWithRegisteredId(91, 100, 600000);// Duplicate of 52
        addLimitWithRegisteredId(92, 3000, 1500000);
        addLimitWithRegisteredId(93, 3000, 300000);
        addLimitWithRegisteredId(94, 30000, 3000000);
        addLimitWithRegisteredId(95, 500, 3000000);
        addLimitWithRegisteredId(96, 100000, 50000000);
        addLimitWithRegisteredId(97, 100000, 10000000);
        addLimitWithRegisteredId(98, 1000000, 100000000);
        addLimitWithRegisteredId(99, 20000, 100000000);
        addLimitWithRegisteredId(100, 300, 30000); // Duplicate of 23
        addLimitWithRegisteredId(101, 50, 300000);
        addLimitWithRegisteredId(102, 2000, 300000);
        addLimitWithRegisteredId(103, 9000, 1500000);
        addLimitWithRegisteredId(104, 300000, 50000000);
        addLimitWithRegisteredId(105, 900, 150000);
        addLimitWithRegisteredId(106, 500, 600000);
        addLimitWithRegisteredId(107, 3000, 3000000);
        addLimitWithRegisteredId(108, 100000, 100000000);
        addLimitWithRegisteredId(109, 300, 300000);
        addLimitWithRegisteredId(110, 5000, 200000);
        addLimitWithRegisteredId(111, 100, 500000);
        addLimitWithRegisteredId(112, 10000, 1500000);
        addLimitWithRegisteredId(113, 20000, 2000000);
        addLimitWithRegisteredId(114, 50, 200000);
        addLimitWithRegisteredId(115, 25000, 2500000);
        addLimitWithRegisteredId(116, 7000, 700000);
        addLimitWithRegisteredId(117, 500000, 150000000);
        addLimitWithRegisteredId(118, 3000000, 300000000);
        addLimitWithRegisteredId(119, 1000, 1000000);
        addLimitWithRegisteredId(120, 500, 150000);
        addLimitWithRegisteredId(121, 500, 150000); // Duplicate of 120
        addLimitWithRegisteredId(122, 10000, 1000000000);
        addLimitWithRegisteredId(123, 150000, 15000000);
        addLimitWithRegisteredId(124, 1000, 100000); // Duplicate of 3
        addLimitWithRegisteredId(125, 300, 100000);
        addLimitWithRegisteredId(126, 15000, 1500000);
        addLimitWithRegisteredId(127, 1500, 150000);
        addLimitWithRegisteredId(128, 2500, 2500);
        addLimitWithRegisteredId(129, 50000, 10000000);
        addLimitWithRegisteredId(130, 30000, 5000000);
        addLimitWithRegisteredId(131, 100, 5000000);
        addLimitWithRegisteredId(132, 100, 1000000);
        addLimitWithRegisteredId(133, 100, 10000000);
        addLimitWithRegisteredId(134, 20, 3000);
        addLimitWithRegisteredId(135, 500, 300000); // Duplicate of 88
        addLimitWithRegisteredId(136, 50000, 12000000);
        addLimitWithRegisteredId(137, 25000, 6000000);
        addLimitWithRegisteredId(138, 50000, 4000000);
        addLimitWithRegisteredId(139, 10000, 4000000);
        addLimitWithRegisteredId(140, 100, 300000);
        addLimitWithRegisteredId(141, 100, 12500);
        addLimitWithRegisteredId(142, 100, 2500);
        addLimitWithRegisteredId(143, 5000, 1500000);
        addLimitWithRegisteredId(144, 100, 500);
        addLimitWithRegisteredId(145, 2500000, 12500000);
        addLimitWithRegisteredId(146, 20000, 2500000);
        addLimitWithRegisteredId(147, 20000, 5000000);
        addLimitWithRegisteredId(148, 500, 5000000);
        addLimitWithRegisteredId(149, 200000, 20000000);
        addLimitWithRegisteredId(150, 6000, 60000);
        addLimitWithRegisteredId(151, 7500, 750000);
        addLimitWithRegisteredId(152, 2250, 375000);
        addLimitWithRegisteredId(153, 750, 75000);
    }

    private Limit(long id, int minValue, int maxValue) {
        this.id = id;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @JsonCreator
    public static Limit valueOf(@JsonProperty("id") long id,
                                @JsonProperty("minValue") int minValue,
                                @JsonProperty("maxValue") int maxValue) {
        Limit limit;
        if (!isRegistered(id)) {
            return valueOf(minValue, maxValue);
        }
        limit = limitsById.get(id);
        if (limit == null) {
            limit = addLimitWithRegisteredId(id, minValue, maxValue);
        }
        return limit;
    }

    public static Limit valueOf(int minValue, int maxValue) {
        Pair<Integer, Integer> pair = new Pair<>(minValue, maxValue);
        Limit limit = limitsByValue.get(pair);
        if (limit == null) {
            limit = addGenericLimit(minValue, maxValue);
        }
        return limit;
    }

    private static Limit addGenericLimit(int minValue, int maxValue) {
        Limit limit = new Limit(GENERIC_LIMIT_ID, minValue, maxValue);
        limitsByValue.put(new Pair<>(minValue, maxValue), limit);
        return limit;
    }

    public static Limit getById(long id) {
        return limitsById.get(id);
    }

    public static Limit getByValue(int minValue, int maxValue) {
        return limitsByValue.get(new Pair<>(minValue, maxValue));
    }

    private static Limit addLimitWithRegisteredId(long id, int minValue, int maxValue) {
        Limit limit = new Limit(id, minValue, maxValue);
        Limit existed = limitsById.putIfAbsent(id, limit);
        if (existed != null) {
            if (!existed.equals(limit)) {
                LOG.error("addLimitWithRegisteredId::Limit with id=" + id + ", already exists, but values different," +
                        " adding generic limit, minValue=" + minValue + ", maxValue=" + maxValue);
                return addGenericLimit(minValue, maxValue);
            }
            LOG.error("Cannot add limit with id=" + id + ", minValue=" + minValue + ", maxValue=" + maxValue +
                    ", return existed object");
            return existed;
        }
        Limit existedByValue = getByValue(minValue, maxValue);
        if (existedByValue != null) {
            LOG.warn("limit:" + limit + ", use the same value as:" + existedByValue + ", skip adding in limitsByValue map");
            return limit;
        }
        limitsByValue.put(new Pair<>(minValue, maxValue), limit);
        return limit;
    }

    /**
     * @return true - registered limit with id; false - generic id, usually: -1 or 0
     */
    private static boolean isRegistered(long id) {
        return id > 0;
    }

    public static List<Limit> getAllRegistered() {
        List<Limit> limits = new ArrayList<>(limitsById.values());
        Collections.sort(limits);
        return limits;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getMinValue() {
        return minValue;
    }

    @Override
    public int getMaxValue() {
        return maxValue;
    }

    public static class LimitSerializer extends Serializer<Limit> {

        @Override
        public void write(Kryo kryo, Output output, Limit limit) {
            output.writeInt(VERSION);
            output.writeLong(limit.getId());
            output.writeInt(limit.getMinValue());
            output.writeInt(limit.getMaxValue());
        }

        @Override
        public Limit read(Kryo kryo, Input input, Class<Limit> aClass) {
            int ver = input.readInt();
            long id = input.readLong();
            int minValue = input.readInt();
            int maxValue = input.readInt();
            return valueOf(id, minValue, maxValue);
        }
    }

    @Override
    public String toString() {
        return "Limit{" +
                "id=" + id +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Limit limit = (Limit) o;

        if (id != limit.id) return false;
        if (minValue != limit.minValue) return false;
        return maxValue == limit.maxValue;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + minValue;
        result = 31 * result + maxValue;
        return result;
    }

    @Override
    public int compareTo(Limit o) {
        return Long.compare(id, o.getId());
    }

    @Override
    public Limit copy() {
        return new Limit(id, minValue, maxValue);
    }
}
