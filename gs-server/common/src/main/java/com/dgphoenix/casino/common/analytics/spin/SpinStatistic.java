package com.abs.casino.common.analytics.spin;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by isador
 * on 10/24/17
 */
public class SpinStatistic implements KryoSerializable {
    private long spinsCount;
    private long spinRequestCount;
    private long spinRequestSum;
    private int spinRequestMin;
    private int spinRequestMax;
    private long clientAnimationCount;
    private long clientAnimationSum;
    private int clientAnimationMin;
    private int clientAnimationMax;
    private long totalSum;
    private int totalMin;
    private int totalMax;
    private long initSum;
    private int initMin;
    private int initMax;
    private long cwBetCount;
    private long cwBetSum;
    private int cwBetMin;
    private int cwBetMax;
    private long gameLogicSum;
    private int gameLogicMin;
    private int gameLogicMax;
    private long cwWinCount;
    private long cwWinSum;
    private int cwWinMin;
    private int cwWinMax;

    private transient long startTimestamp;

    public SpinStatistic() {
    }

    public SpinStatistic(long spinsCount, long spinRequestCount, long spinRequestSum, int spinRequestMin,
                         int spinRequestMax, long clientAnimationCount, long clientAnimationSum, int clientAnimationMin,
                         int clientAnimationMax, long totalSum, int totalMin, int totalMax, long initSum, int initMin,
                         int initMax, long cwBetCount, long cwBetSum, int cwBetMin, int cwBetMax, long gameLogicSum,
                         int gameLogicMin, int gameLogicMax, long cwWinCount, long cwWinSum, int cwWinMin, int cwWinMax) {
        this.spinsCount = spinsCount;
        this.spinRequestCount = spinRequestCount;
        this.spinRequestSum = spinRequestSum;
        this.spinRequestMin = spinRequestMin;
        this.spinRequestMax = spinRequestMax;
        this.clientAnimationCount = clientAnimationCount;
        this.clientAnimationSum = clientAnimationSum;
        this.clientAnimationMin = clientAnimationMin;
        this.clientAnimationMax = clientAnimationMax;
        this.totalSum = totalSum;
        this.totalMin = totalMin;
        this.totalMax = totalMax;
        this.initSum = initSum;
        this.initMin = initMin;
        this.initMax = initMax;
        this.cwBetCount = cwBetCount;
        this.cwBetSum = cwBetSum;
        this.cwBetMin = cwBetMin;
        this.cwBetMax = cwBetMax;
        this.gameLogicSum = gameLogicSum;
        this.gameLogicMin = gameLogicMin;
        this.gameLogicMax = gameLogicMax;
        this.cwWinCount = cwWinCount;
        this.cwWinSum = cwWinSum;
        this.cwWinMin = cwWinMin;
        this.cwWinMax = cwWinMax;
    }

    public void aggregate(Iterable<SpinStatistic> spinStatistics) {
        spinStatistics.forEach(this::aggregate);
    }

    public void aggregate(SpinStatistic spinStatistic) {
        incrementSpinsCount(spinStatistic.spinsCount);
        incrementSpinRequestCount(spinStatistic.spinRequestCount);
        increaseSpinRequestSum(spinStatistic.spinRequestSum);
        setSpinRequestMin(spinStatistic.spinRequestMin);
        setSpinRequestMax(spinStatistic.spinRequestMax);
        incrementClientAnimationCount(spinStatistic.clientAnimationCount);
        increaseClientAnimationSum(spinStatistic.clientAnimationSum);
        setClientAnimationMin(spinStatistic.clientAnimationMin);
        setClientAnimationMax(spinStatistic.clientAnimationMax);
        increaseTotalSum(spinStatistic.totalSum);
        setTotalMin(spinStatistic.totalMin);
        setTotalMax(spinStatistic.totalMax);
        increaseInitSum(spinStatistic.initSum);
        setInitMin(spinStatistic.initMin);
        setInitMax(spinStatistic.initMax);
        incrementCwBetCount(spinStatistic.cwBetCount);
        increaseCwBetSum(spinStatistic.cwBetSum);
        setCwBetMin(spinStatistic.cwBetMin);
        setCwBetMax(spinStatistic.cwBetMax);
        increaseGameLogicSum(spinStatistic.gameLogicSum);
        setGameLogicMin(spinStatistic.gameLogicMin);
        setGameLogicMax(spinStatistic.gameLogicMax);
        incrementCwWinCount(spinStatistic.cwWinCount);
        increaseCwWinSum(spinStatistic.cwWinSum);
        setCwWinMin(spinStatistic.cwWinMin);
        setCwWinMax(spinStatistic.cwWinMax);
    }

    public static SpinStatistic reduce(SpinStatistic spinStatistic1, SpinStatistic spinStatistic2) {
        if (spinStatistic2 == null) {
            return spinStatistic1;
        } else if (spinStatistic1 == null) {
            return spinStatistic2;
        }
        spinStatistic1.aggregate(spinStatistic2);
        return spinStatistic1;
    }

    public void setClientStatistic(ClientStatistic clientStatistic) {
        clientStatistic.getSpinAnimTime().ifPresent(this::setClientAnimation);
        clientStatistic.getSpinReqTime().ifPresent(this::setSpinRequest);
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public void setSpinRequest(int spinRequest) {
        incrementSpinRequestCount(1);
        increaseSpinRequestSum(spinRequest);
        setSpinRequestMin(spinRequest);
        setSpinRequestMax(spinRequest);
    }

    public void incrementSpinRequestCount(long spinRequestCount) {
        this.spinRequestCount += spinRequestCount;
    }

    private void increaseSpinRequestSum(long spinRequest) {
        spinRequestSum += spinRequest;
    }

    public void setSpinRequestMin(int spinRequest) {
        spinRequestMin = getMin(spinRequestMin, spinRequest);
    }

    public void setSpinRequestMax(int spinRequest) {
        spinRequestMax = max(spinRequestMax, spinRequest);
    }

    public void setClientAnimation(int clientAnimation) {
        incrementClientAnimationCount(1);
        increaseClientAnimationSum(clientAnimation);
        setClientAnimationMin(clientAnimation);
        setClientAnimationMax(clientAnimation);
    }

    public void incrementClientAnimationCount(long clientAnimationCount) {
        this.clientAnimationCount += clientAnimationCount;
    }

    private void increaseClientAnimationSum(long clientAnimation) {
        clientAnimationSum += clientAnimation;
    }

    public void setClientAnimationMin(int clientAnimation) {
        clientAnimationMin = getMin(clientAnimationMin, clientAnimation);
    }

    public void setClientAnimationMax(int clientAnimation) {
        clientAnimationMax = max(clientAnimationMax, clientAnimation);
    }

    public void setTotal(int total) {
        incrementSpinsCount(1);
        increaseTotalSum(total);
        setTotalMin(total);
        setTotalMax(total);
    }

    public void incrementSpinsCount(long spinsCount) {
        this.spinsCount += spinsCount;
    }

    private void increaseTotalSum(long total) {
        totalSum += total;
    }

    public void setTotalMin(int total) {
        totalMin = getMin(totalMin, total);
    }

    public void setTotalMax(int total) {
        totalMax = max(totalMax, total);
    }

    public void setInit(int init) {
        increaseInitSum(init);
        setInitMin(init);
        setInitMax(init);
    }

    private void increaseInitSum(long init) {
        initSum += init;
    }

    public void setInitMin(int init) {
        initMin = getMin(initMin, init);
    }

    public void setInitMax(int init) {
        initMax = max(initMax, init);
    }

    public void setCwBet(int cwBet) {
        incrementCwBetCount(1);
        increaseCwBetSum(cwBet);
        setCwBetMin(cwBet);
        setCwBetMax(cwBet);
    }

    public void incrementCwBetCount(long cwBetCount) {
        this.cwBetCount += cwBetCount;
    }

    private void increaseCwBetSum(long cwBet) {
        cwBetSum += cwBet;
    }

    public void setCwBetMin(int cwBet) {
        cwBetMin = getMin(cwBetMin, cwBet);
    }

    public void setCwBetMax(int cwBet) {
        cwBetMax = max(cwBetMax, cwBet);
    }

    public void setGameLogic(int gameLogic) {
        increaseGameLogicSum(gameLogic);
        setGameLogicMin(gameLogic);
        setGameLogicMax(gameLogic);
    }

    private void increaseGameLogicSum(long gameLogic) {
        gameLogicSum += gameLogic;
    }

    public void setGameLogicMin(int gameLogic) {
        gameLogicMin = getMin(gameLogicMin, gameLogic);
    }

    public void setGameLogicMax(int gameLogic) {
        gameLogicMax = max(gameLogicMax, gameLogic);
    }

    public void setCwWin(int cwWin) {
        incrementCwWinCount(1);
        increaseCwWinSum(cwWin);
        setCwWinMin(cwWin);
        setCwWinMax(cwWin);
    }

    public void incrementCwWinCount(long cwWinCount) {
        this.cwWinCount += cwWinCount;
    }

    private void increaseCwWinSum(long cwWin) {
        cwWinSum += cwWin;
    }

    public void setCwWinMin(int cwWin) {
        cwWinMin = getMin(cwWinMin, cwWin);
    }

    public void setCwWinMax(int cwWin) {
        cwWinMax = max(cwWinMax, cwWin);
    }

    private int getMin(int value1, int value2) {
        if (value1 == 0) {
            return value2;
        }
        if (value2 == 0) {
            return value1;
        }
        return min(value1, value2);
    }

    private long getAvg(long sum, long count) {
        return count != 0 ? sum / count : 0;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getSpinsCount() {
        return spinsCount;
    }

    public long getSpinRequestCount() {
        return spinRequestCount;
    }

    public long getSpinRequestSum() {
        return spinRequestSum;
    }

    public int getSpinRequestMin() {
        return spinRequestMin;
    }

    public int getSpinRequestMax() {
        return spinRequestMax;
    }

    public long getSpinRequestAvg() {
        return getAvg(spinRequestSum, spinRequestCount);
    }

    public long getClientAnimationCount() {
        return clientAnimationCount;
    }

    public long getClientAnimationSum() {
        return clientAnimationSum;
    }

    public int getClientAnimationMin() {
        return clientAnimationMin;
    }

    public int getClientAnimationMax() {
        return clientAnimationMax;
    }

    public long getClientAnimationAvg() {
        return getAvg(clientAnimationSum, clientAnimationCount);
    }

    public long getTotalSum() {
        return totalSum;
    }

    public int getTotalMin() {
        return totalMin;
    }

    public int getTotalMax() {
        return totalMax;
    }

    public long getTotalAvg() {
        return getAvg(totalSum, spinsCount);
    }

    public long getInitSum() {
        return initSum;
    }

    public int getInitMin() {
        return initMin;
    }

    public int getInitMax() {
        return initMax;
    }

    public long getInitAvg() {
        return getAvg(initSum, spinsCount);
    }

    public long getCwBetCount() {
        return cwBetCount;
    }

    public long getCwBetSum() {
        return cwBetSum;
    }

    public int getCwBetMin() {
        return cwBetMin;
    }

    public int getCwBetMax() {
        return cwBetMax;
    }

    public long getCwBetAvg() {
        return getAvg(cwBetSum, cwBetCount);
    }

    public long getGameLogicSum() {
        return gameLogicSum;
    }

    public int getGameLogicMin() {
        return gameLogicMin;
    }

    public int getGameLogicMax() {
        return gameLogicMax;
    }

    public long getGameLogicAvg() {
        return getAvg(gameLogicSum, spinsCount);
    }

    public long getCwWinCount() {
        return cwWinCount;
    }

    public long getCwWinSum() {
        return cwWinSum;
    }

    public int getCwWinMin() {
        return cwWinMin;
    }

    public int getCwWinMax() {
        return cwWinMax;
    }

    public long getCwWinAvg() {
        return getAvg(cwWinSum, cwWinCount);
    }

    @Override
    public String toString() {
        return "SpinStatistic[" +
                "spinsCount=" + spinsCount +
                ", spinRequestSum=" + spinRequestSum +
                ", spinRequestMin=" + spinRequestMin +
                ", spinRequestMax=" + spinRequestMax +
                ", clientAnimationSum=" + clientAnimationSum +
                ", clientAnimationMin=" + clientAnimationMin +
                ", clientAnimationMax=" + clientAnimationMax +
                ", totalSum=" + totalSum +
                ", totalMin=" + totalMin +
                ", totalMax=" + totalMax +
                ", initSum=" + initSum +
                ", initMin=" + initMin +
                ", initMax=" + initMax +
                ", cwBetCount=" + cwBetCount +
                ", cwBetSum=" + cwBetSum +
                ", cwBetMin=" + cwBetMin +
                ", cwBetMax=" + cwBetMax +
                ", gameLogicSum=" + gameLogicSum +
                ", gameLogicMin=" + gameLogicMin +
                ", gameLogicMax=" + gameLogicMax +
                ", cwWinCount=" + cwWinCount +
                ", cwWinSum=" + cwWinSum +
                ", cwWinMin=" + cwWinMin +
                ", cwWinMax=" + cwWinMax +
                ']';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeLong(spinsCount, true);
        output.writeLong(spinRequestCount, true);
        output.writeLong(spinRequestSum, true);
        output.writeInt(spinRequestMin, true);
        output.writeInt(spinRequestMax, true);
        output.writeLong(clientAnimationCount, true);
        output.writeLong(clientAnimationSum, true);
        output.writeInt(clientAnimationMin, true);
        output.writeInt(clientAnimationMax, true);
        output.writeLong(totalSum, true);
        output.writeInt(totalMin, true);
        output.writeInt(totalMax, true);
        output.writeLong(initSum, true);
        output.writeInt(initMin, true);
        output.writeInt(initMax, true);
        output.writeLong(cwBetCount, true);
        output.writeLong(cwBetSum, true);
        output.writeInt(cwBetMin, true);
        output.writeInt(cwBetMax, true);
        output.writeLong(gameLogicSum, true);
        output.writeInt(gameLogicMin, true);
        output.writeInt(gameLogicMax, true);
        output.writeLong(cwWinCount, true);
        output.writeLong(cwWinSum, true);
        output.writeInt(cwWinMin, true);
        output.writeInt(cwWinMax, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        spinsCount = input.readLong(true);
        spinRequestCount = input.readLong(true);
        spinRequestSum = input.readLong(true);
        spinRequestMin = input.readInt(true);
        spinRequestMax = input.readInt(true);
        clientAnimationCount = input.readLong(true);
        clientAnimationSum = input.readLong(true);
        clientAnimationMin = input.readInt(true);
        clientAnimationMax = input.readInt(true);
        totalSum = input.readLong(true);
        totalMin = input.readInt(true);
        totalMax = input.readInt(true);
        initSum = input.readLong(true);
        initMin = input.readInt(true);
        initMax = input.readInt(true);
        cwBetCount = input.readLong(true);
        cwBetSum = input.readLong(true);
        cwBetMin = input.readInt(true);
        cwBetMax = input.readInt(true);
        gameLogicSum = input.readLong(true);
        gameLogicMin = input.readInt(true);
        gameLogicMax = input.readInt(true);
        cwWinCount = input.readLong(true);
        cwWinSum = input.readLong(true);
        cwWinMin = input.readInt(true);
        cwWinMax = input.readInt(true);
    }
}
