package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.cache.JsonSelfSerializable;
import com.abs.casino.common.util.DatePeriod;
import com.dgphoenix.casino.common.util.ITimeProvider;
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
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 22.11.16.
 */
public class DesiredPrize implements KryoSerializable,
        JsonSelfSerializable<DesiredPrize>, Serializable {
    private static final byte VERSION = 4;

    public static final Comparator<DesiredPrize> BY_START_DATE = new Comparator<DesiredPrize>() {
        @Override
        public int compare(DesiredPrize o1, DesiredPrize o2) {
            return Long.signum(o1.getStartDate() - o2.getStartDate());
        }
    };

    private long promoPrizeId;
    //start date when the prize may be won
    private long startDate;
    //end date when the prize may be won, null for no limit
    private Long endDate;
    private PrizeStatus status;

    private transient int prevReceivedPrizesCount;
    private int receivedPrizesCount;

    //current values (from last win)
    private int qualifiedBetsCount;
    private long qualifiedBetSum;
    private long qualifiedWinSum;

    private int totalQualifiedBetsCount;
    private long totalQualifiedBetSum;
    private long totalQualifiedWinSum;
    //key is gameId, value is current progress
    private Map<Long, Integer> missionsProgress;
    private Map<Long, RoundQualificationStat> roundQualification;
    private long maxQualifiedWin;
    private double maxQualifiedRtp;
    private transient ITimeProvider timeProvider;

    public DesiredPrize() {
    }

    public DesiredPrize(Long promoPrizeId, long startDate, Long endDate) {
        this.promoPrizeId = promoPrizeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = PrizeStatus.ACTIVE;
    }

    public DesiredPrize nextPrize(DatePeriod campaignPeriod, Long limitPeriodInSeconds, Integer limitPerPlayerOnPeriod) {
        DesiredPrize copy = new DesiredPrize();
        copy.promoPrizeId = promoPrizeId;
        copy.startDate = getCurrentTime();
        if (endDate != null && limitPeriodInSeconds != null) {
            copy.endDate = copy.startDate + (limitPeriodInSeconds * 1000);
            if (copy.endDate > campaignPeriod.getEndDate().getTime()) {
                copy.endDate = campaignPeriod.getEndDate().getTime();
            }
        }
        copy.status = PrizeStatus.ACTIVE;
        if (limitPerPlayerOnPeriod == null || receivedPrizesCount < limitPerPlayerOnPeriod) {
            copy.qualifiedBetsCount = qualifiedBetsCount;
            copy.qualifiedBetSum = qualifiedBetSum;
            copy.qualifiedWinSum = qualifiedWinSum;
        } else {
            copy.qualifiedBetsCount = 0;
            copy.qualifiedBetSum = 0;
            copy.qualifiedWinSum = 0;
        }
        copy.receivedPrizesCount = 0;
        copy.totalQualifiedBetsCount = totalQualifiedBetsCount;
        copy.totalQualifiedBetSum = totalQualifiedBetSum;
        copy.totalQualifiedWinSum = totalQualifiedWinSum;
        if (roundQualification != null) {
            copy.roundQualification = new HashMap<Long, RoundQualificationStat>(roundQualification);
        }
        copy.maxQualifiedWin = maxQualifiedWin;
        copy.maxQualifiedRtp = maxQualifiedRtp;
        return copy;
    }

    public long getPromoPrizeId() {
        return promoPrizeId;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public void resetCurrentProgress(int betsCount, long betSum, long winSum) {
        qualifiedBetsCount = betsCount;
        qualifiedBetSum = betSum;
        qualifiedWinSum = winSum;
    }

    public boolean isInPeriod() {
        long now = getCurrentTime();
        return now >= startDate && (endDate == null || now <= endDate);
    }

    public boolean isPastPeriod() {
        return endDate != null && getCurrentTime() > endDate;
    }

    public PrizeStatus getStatus() {
        return status == null ? PrizeStatus.ACTIVE : status;
    }

    public void setStatus(PrizeStatus status) {
        this.status = status;
    }

    public void updateBets(int incBetsCount, long incBetsSum) {
        qualifiedBetsCount += incBetsCount;
        qualifiedBetSum += incBetsSum;
        totalQualifiedBetsCount += incBetsCount;
        totalQualifiedBetSum += incBetsSum;
    }

    public void updateWins(long incWinSum) {
        qualifiedWinSum += incWinSum;
        totalQualifiedWinSum += incWinSum;
    }

    public int getQualifiedBetsCount() {
        return qualifiedBetsCount;
    }

    public void setQualifiedBetsCount(int qualifiedBetsCount) {
        this.qualifiedBetsCount = qualifiedBetsCount;
    }

    public long getQualifiedBetSum() {
        return qualifiedBetSum;
    }

    public void setQualifiedBetSum(long qualifiedBetSum) {
        this.qualifiedBetSum = qualifiedBetSum;
    }

    public void setQualifiedWinSum(long qualifiedWinSum) {
        this.qualifiedWinSum = qualifiedWinSum;
    }

    public void setTotalQualifiedBetsCount(int totalQualifiedBetsCount) {
        this.totalQualifiedBetsCount = totalQualifiedBetsCount;
    }

    public void setTotalQualifiedBetSum(long totalQualifiedBetSum) {
        this.totalQualifiedBetSum = totalQualifiedBetSum;
    }

    public void setTotalQualifiedWinSum(long totalQualifiedWinSum) {
        this.totalQualifiedWinSum = totalQualifiedWinSum;
    }

    public long getQualifiedWinSum() {
        return qualifiedWinSum;
    }

    public int getTotalQualifiedBetsCount() {
        return totalQualifiedBetsCount;
    }

    public long getTotalQualifiedBetSum() {
        return totalQualifiedBetSum;
    }

    public long getTotalQualifiedWinSum() {
        return totalQualifiedWinSum;
    }

    public int getReceivedPrizesCount() {
        return receivedPrizesCount;
    }

    public void incrementReceivedPrizesCount() {
        prevReceivedPrizesCount = receivedPrizesCount;
        receivedPrizesCount++;
    }

    public void incrementReceivedPrizesCount(int qualifiedPrizesAtOnce) {
        prevReceivedPrizesCount = receivedPrizesCount;
        receivedPrizesCount += qualifiedPrizesAtOnce;
    }

    public void setReceivedPrizesCount(int receivedPrizesCount) {
        this.prevReceivedPrizesCount = receivedPrizesCount;
        this.receivedPrizesCount = receivedPrizesCount;
    }

    public int getPrevReceivedPrizesCount() {
        return prevReceivedPrizesCount;
    }

    public void setPrevReceivedPrizesCount(int prevReceivedPrizesCount) {
        this.prevReceivedPrizesCount = prevReceivedPrizesCount;
    }

    public Map<Long, Integer> getMissionsProgress() {
        return missionsProgress;
    }

    public int getCurrentProgress(long gameId) {
        Integer progress = missionsProgress == null ? null : missionsProgress.get(gameId);
        return progress == null ? 0 : progress;
    }

    public void setCurrentProgress(long gameId, int progress) {
        if (missionsProgress == null) {
            missionsProgress = new HashMap<Long, Integer>();
        }
        missionsProgress.put(gameId, progress);
    }

    public void incrementCurrentProgress(long gameId, int progress) {
        if (missionsProgress == null) {
            missionsProgress = new HashMap<Long, Integer>();
        }
        Integer currentProgress = missionsProgress.get(gameId);
        if (currentProgress == null) {
            missionsProgress.put(gameId, progress);
        } else {
            missionsProgress.put(gameId, currentProgress + progress);
        }
    }

    public Map<Long, RoundQualificationStat> getRoundQualification() {
        return roundQualification;
    }

    public void setRoundBet(long gameId, long bet) {
        if (roundQualification == null) {
            roundQualification = new HashMap<Long, RoundQualificationStat>();
        }
        RoundQualificationStat stat = roundQualification.get(gameId);
        if (stat == null) {
            stat = new RoundQualificationStat(bet);
            roundQualification.put(gameId, stat);
        }
        stat.setBetAmount(bet);
    }

    public void incrementRoundWin(long gameId, long win) {
        if (roundQualification == null) {
            roundQualification = new HashMap<Long, RoundQualificationStat>();
        }
        RoundQualificationStat stat = roundQualification.get(gameId);
        if (stat == null) {
            stat = new RoundQualificationStat(0L);
            roundQualification.put(gameId, stat);
        }
        stat.incrementRoundSummaryWin(win);
    }

    public boolean isBetQualified(long gameId) {
        RoundQualificationStat stat = roundQualification == null ? null : roundQualification.get(gameId);
        return stat != null && stat.isBetQualified();
    }

    public void resetRoundQualification(long gameId) {
        RoundQualificationStat stat = roundQualification == null ? null : roundQualification.get(gameId);
        if (stat != null) {
            stat.reset();
        }
    }

    public long getMaxQualifiedWin() {
        return maxQualifiedWin;
    }

    public double getMaxQualifiedRtp() {
        return maxQualifiedRtp;
    }

    public void updateMaxQualifiedWin(long win) {
        this.maxQualifiedWin = Math.max(maxQualifiedWin, win);
    }

    public void updateMaxQualifiedRtp(double rtp) {
        this.maxQualifiedRtp = Math.max(maxQualifiedRtp, rtp);
    }

    public void updateMaxQualifiedRoundWin(long gameId) {
        RoundQualificationStat stat = roundQualification == null ? null : roundQualification.get(gameId);
        if (stat != null) {
            updateMaxQualifiedWin(stat.getRoundSummaryWin());
        }
    }

    public void updateMaxQualifiedRoundRtp(long gameId) {
        RoundQualificationStat stat = roundQualification == null ? null : roundQualification.get(gameId);
        if (stat != null) {
            updateMaxQualifiedRtp(stat.getRoundRtp());
        }
    }

    private long getCurrentTime() {
        if (timeProvider != null) {
            return timeProvider.getTime();
        }
        return System.currentTimeMillis();
    }

    public void setTimeProvider(ITimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DesiredPrize that = (DesiredPrize) o;

        if (startDate != that.startDate) return false;
        if (status != that.status) return false;
        if (qualifiedBetsCount != that.qualifiedBetsCount) return false;
        if (qualifiedBetSum != that.qualifiedBetSum) return false;
        if (qualifiedWinSum != that.qualifiedWinSum) return false;
        return promoPrizeId == that.promoPrizeId;

    }

    @Override
    public int hashCode() {
        int result = (int) (promoPrizeId ^ (promoPrizeId >>> 32));
        result = 31 * result + (int) (startDate ^ (startDate >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "DesiredPrize[" +
                "promoPrizeId=" + promoPrizeId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                ", receivedPrizesCount=" + receivedPrizesCount +
                ", prevReceivedPrizesCount=" + prevReceivedPrizesCount +
                ", qualifiedBetsCount=" + qualifiedBetsCount +
                ", qualifiedBetSum=" + qualifiedBetSum +
                ", qualifiedWinSum=" + qualifiedWinSum +
                ", totalQualifiedBetsCount=" + totalQualifiedBetsCount +
                ", totalQualifiedBetSum=" + totalQualifiedBetSum +
                ", totalQualifiedWinSum=" + totalQualifiedWinSum +
                ", missionsProgress=" + missionsProgress +
                ", roundQualification=" + roundQualification +
                ", maxQualifiedWin=" + maxQualifiedWin +
                ", maxQualifiedRtp=" + maxQualifiedRtp +
                ']';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(promoPrizeId, true);
        output.writeLong(startDate, true);
        kryo.writeObjectOrNull(output, endDate, Long.class);
        output.writeString(status.name());
        output.writeInt(receivedPrizesCount, true);
        output.writeInt(qualifiedBetsCount, true);
        output.writeLong(qualifiedBetSum, true);
        output.writeLong(qualifiedWinSum, true);
        output.writeInt(totalQualifiedBetsCount, true);
        output.writeLong(totalQualifiedBetSum, true);
        output.writeLong(totalQualifiedWinSum, true);
        kryo.writeClassAndObject(output, missionsProgress);
        output.writeInt(prevReceivedPrizesCount, true);
        kryo.writeClassAndObject(output, roundQualification);
        output.writeLong(maxQualifiedWin, true);
        output.writeDouble(maxQualifiedRtp);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        promoPrizeId = input.readLong(true);
        startDate = input.readLong(true);
        endDate = kryo.readObjectOrNull(input, Long.class);
        String s = input.readString();
        status = PrizeStatus.valueOf(s);
        receivedPrizesCount = input.readInt(true);
        qualifiedBetsCount = input.readInt(true);
        qualifiedBetSum = input.readLong(true);
        qualifiedWinSum = input.readLong(true);
        totalQualifiedBetsCount = input.readInt(true);
        totalQualifiedBetSum = input.readLong(true);
        totalQualifiedWinSum = input.readLong(true);
        //noinspection unchecked
        missionsProgress = (Map<Long, Integer>) kryo.readClassAndObject(input);
        if(ver > 0) {
            prevReceivedPrizesCount = input.readInt(true);
        }
        if (ver > 1) {
            roundQualification =  (Map<Long, RoundQualificationStat>) kryo.readClassAndObject(input);
        }
        if(ver > 2) {
            maxQualifiedWin = input.readLong(true);
        }
        if(ver > 3) {
            maxQualifiedRtp = input.readDouble();
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("promoPrizeId", promoPrizeId);
        gen.writeNumberField("startDate", startDate);
        serializeNumberOrNull(gen, "endDate", endDate);
        gen.writeStringField("status", status.name());
        gen.writeNumberField("receivedPrizesCount", receivedPrizesCount);
        gen.writeNumberField("qualifiedBetsCount", qualifiedBetsCount);
        gen.writeNumberField("qualifiedBetSum", qualifiedBetSum);
        gen.writeNumberField("qualifiedWinSum", qualifiedWinSum);
        gen.writeNumberField("totalQualifiedBetsCount", totalQualifiedBetsCount);
        gen.writeNumberField("totalQualifiedBetSum", totalQualifiedBetSum);
        gen.writeNumberField("totalQualifiedWinSum", totalQualifiedWinSum);
        serializeMapField(gen, "missionsProgress", missionsProgress, new TypeReference<Map<Long,Integer>>() {});
        gen.writeNumberField("prevReceivedPrizesCount", prevReceivedPrizesCount);
        serializeMapField(gen, "roundQualification", roundQualification, new TypeReference<Map<Long,RoundQualificationStat>>() {});
        gen.writeNumberField("maxQualifiedWin", maxQualifiedWin);
        gen.writeNumberField("maxQualifiedRtp", maxQualifiedRtp);
    }

    @Override
    public DesiredPrize deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        JsonNode node = p.getCodec().readTree(p);

        promoPrizeId = node.get("promoPrizeId").longValue();
        startDate = node.get("startDate").longValue();
        endDate = deserializeOrNull(om, node.get("endDate"), Long.class);
        status = PrizeStatus.valueOf(readNullableText(node, "status"));
        receivedPrizesCount = node.get("receivedPrizesCount").asInt();
        qualifiedBetsCount = node.get("qualifiedBetsCount").asInt();
        qualifiedBetSum = node.get("qualifiedBetSum").asLong();
        qualifiedWinSum = node.get("qualifiedWinSum").asLong();
        totalQualifiedBetsCount = node.get("totalQualifiedBetsCount").asInt();
        totalQualifiedBetSum = node.get("totalQualifiedBetSum").asLong();
        totalQualifiedWinSum = node.get("totalQualifiedWinSum").asLong();
        //noinspection unchecked
        missionsProgress = om.treeToValue(node.get("missionsProgress"), new TypeReference<Map<Long, Integer>>() {});
        prevReceivedPrizesCount = node.get("prevReceivedPrizesCount").asInt();
        roundQualification = om.treeToValue(node.get("roundQualification"), new TypeReference<Map<Long, RoundQualificationStat>>() {});
        maxQualifiedWin = node.get("maxQualifiedWin").longValue();
        maxQualifiedRtp = node.get("maxQualifiedRtp").doubleValue();

        return this;
    }
}
