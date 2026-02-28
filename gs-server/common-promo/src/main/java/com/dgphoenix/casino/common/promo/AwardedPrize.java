package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.cache.Identifiable;
import com.abs.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
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
import java.util.*;

/**
 * User: flsh
 * Date: 22.11.16.
 */
public class AwardedPrize implements KryoSerializable, 
        JsonSelfSerializable<AwardedPrize>, Identifiable {
    private static final byte VERSION = 1;

    public static final Comparator<AwardedPrize> BY_AWARD_DATE = new Comparator<AwardedPrize>() {
        @Override
        public int compare(AwardedPrize o1, AwardedPrize o2) {
            int byDateCompare = Long.signum(o1.getAwardDate() - o2.getAwardDate());
            return byDateCompare != 0 ? byDateCompare : Long.signum(o1.getId() - o2.getId());
        }
    };

    private long id;
    private long promoPrizeId;
    private long awardDate;

    //current values at the time of winning
    private int betsCount;
    private long betSum;
    private long winSum;
    private Set<Long> unsentNotificationIds = new HashSet<Long>();
    //key is unsentNotificationId, value is awardedCount.  if no value in this map, awardedCount=1
    private transient Map<Long, Integer> usendedNotificationsAwardedCount = new HashMap<Long, Integer>();
    //this is total count
    private int highFrequencyAwardedCount;

    private AwardedPrize() {
    }

    public AwardedPrize(long id, long promoPrizeId, long awardDate) {
        this.id = id;
        this.promoPrizeId = promoPrizeId;
        this.awardDate = awardDate;
        this.unsentNotificationIds.add(id);
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void updateStatistics(DesiredPrize desiredPrize) {
        this.betsCount = desiredPrize.getQualifiedBetsCount();
        this.betSum = desiredPrize.getQualifiedBetSum();
        this.winSum = desiredPrize.getQualifiedWinSum();
    }

    public long getPromoPrizeId() {
        return promoPrizeId;
    }

    public long getAwardDate() {
        return awardDate;
    }

    public void setAwardDate(long awardDate) {
        this.awardDate = awardDate;
    }

    public int getBetsCount() {
        return betsCount;
    }

    public void setBetsCount(int betsCount) {
        this.betsCount = betsCount;
    }

    public long getBetSum() {
        return betSum;
    }

    public void setBetSum(long betSum) {
        this.betSum = betSum;
    }

    public long getWinSum() {
        return winSum;
    }

    public void setWinSum(long winSum) {
        this.winSum = winSum;
    }

    public int getHighFrequencyAwardedCount() {
        return highFrequencyAwardedCount;
    }

    public void setHighFrequencyAwardedCount(int highFrequencyAwardedCount) {
        this.highFrequencyAwardedCount = highFrequencyAwardedCount;
    }

    public void incrementHighFrequencyAwardedCount() {
        this.highFrequencyAwardedCount++;
    }

    public Set<Long> getUnsentNotificationIds() {
        return unsentNotificationIds;
    }

    public void addNotSendedNotificationId(long notificationId) {
        this.unsentNotificationIds.add(notificationId);
    }

    public boolean isNotificationNotSent(long notificationId) {
        return unsentNotificationIds.contains(notificationId);
    }

    public void setNotificationSent(long notificationId) {
        unsentNotificationIds.remove(notificationId);
        if(usendedNotificationsAwardedCount != null) {
            usendedNotificationsAwardedCount.remove(notificationId);
        }
    }

    public void setUsendedNotificationsAwardedCount(long notificationId, int awardedCount) {
        //usendedNotificationsAwardedCount may be null on SB branch, because marked as TC HonorTransient
        if(usendedNotificationsAwardedCount == null) {
            usendedNotificationsAwardedCount = new HashMap<Long, Integer>();
        }
        usendedNotificationsAwardedCount.put(notificationId, awardedCount);
    }

    public int getUsendedNotificationsAwardedCount(long notificationId) {
        Integer awardedCount = usendedNotificationsAwardedCount == null ? null :
                usendedNotificationsAwardedCount.get(notificationId);
        return awardedCount == null ? 1 : awardedCount;
    }

    public Map<Long, Integer> getUsendedNotificationsAwardedCount() {
        return usendedNotificationsAwardedCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AwardedPrize that = (AwardedPrize) o;

        if (id != that.id) return false;
        if (promoPrizeId != that.promoPrizeId) return false;
        return awardDate == that.awardDate;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (promoPrizeId ^ (promoPrizeId >>> 32));
        result = 31 * result + (int) (awardDate ^ (awardDate >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "AwardedPrize[" +
                "id=" + id +
                ", promoPrizeId=" + promoPrizeId +
                ", awardDate=" + awardDate +
                ", betsCount=" + betsCount +
                ", betSum=" + betSum +
                ", winSum=" + winSum +
                ", highFrequencyAwardedCount=" + highFrequencyAwardedCount +
                ", unsentNotificationIds=" + unsentNotificationIds +
                ", usendedNotificationsAwardedCount=" + usendedNotificationsAwardedCount +
                ']';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(promoPrizeId, true);
        output.writeLong(awardDate, true);
        output.writeInt(betsCount, true);
        output.writeLong(betSum, true);
        output.writeLong(winSum, true);
        output.writeInt(highFrequencyAwardedCount, true);
        kryo.writeClassAndObject(output, unsentNotificationIds);
        kryo.writeClassAndObject(output, usendedNotificationsAwardedCount);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong(true);
        promoPrizeId = input.readLong(true);
        awardDate = input.readLong(true);
        betsCount = input.readInt(true);
        betSum = input.readLong(true);
        winSum = input.readLong(true);
        highFrequencyAwardedCount = input.readInt(true);
        //noinspection unchecked
        unsentNotificationIds = (Set<Long>) kryo.readClassAndObject(input);
        if(ver > 0) {
            //noinspection unchecked
            usendedNotificationsAwardedCount = (Map<Long, Integer>) kryo.readClassAndObject(input);
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("id", id);
        gen.writeNumberField("promoPrizeId", promoPrizeId);
        gen.writeNumberField("awardDate", awardDate);
        gen.writeNumberField("betsCount", betsCount);
        gen.writeNumberField("betSum", betSum);
        gen.writeNumberField("winSum", winSum);
        gen.writeNumberField("highFrequencyAwardedCount", highFrequencyAwardedCount);
        serializeSetField(gen, "unsentNotificationIds", unsentNotificationIds, new TypeReference<Set<Long>>() {});
        serializeMapField(gen, "usendedNotificationsAwardedCount", usendedNotificationsAwardedCount, new TypeReference<Map<Long,Integer>>() {});
    }

    @Override
    public AwardedPrize deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = p.getCodec().readTree(p);

        id = node.get("id").asLong();
        promoPrizeId = node.get("promoPrizeId").asLong();
        awardDate = node.get("awardDate").asLong();
        betsCount = node.get("betsCount").asInt();
        betSum = node.get("betSum").asLong();
        winSum = node.get("winSum").asLong();
        highFrequencyAwardedCount = node.get("highFrequencyAwardedCount").asInt();

        JsonNode unsentNotificationIdsNode = node.get("unsentNotificationIds");
        unsentNotificationIds = unsentNotificationIdsNode != null ? mapper.convertValue(node.get("unsentNotificationIds"), new TypeReference<Set<Long>>() {}) : null;

        JsonNode usendedNotificationsAwardedCountNode = node.get("usendedNotificationsAwardedCount");
        usendedNotificationsAwardedCount = usendedNotificationsAwardedCountNode != null ? mapper.convertValue(node.get("usendedNotificationsAwardedCount"), new TypeReference<Map<Long, Integer>>() {}) : null;
        return this;
    }

}
