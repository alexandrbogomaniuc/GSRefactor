package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.battleground.IRoomBattlegroundInfo;
import com.betsoft.casino.mp.model.battleground.ITransportObserver;

import java.io.Serializable;
import java.util.List;

public class RoomBattlegroundInfo implements IRoomBattlegroundInfo, Serializable {
    private long buyIn;
    private boolean buyInConfirmed;
    private int timeToStart; //seconds
    private List<Integer> kingsOfHill;
    private long score;
    private long rank;
    private long pot;
    private double potTaxPercent;
    private String joinUrl;
    private List<Integer> confirmedSeatsId;
    private List<ITransportObserver> observers;

    public RoomBattlegroundInfo(long buyIn, boolean buyInConfirmed, int timeToStart,
                                List<Integer> kingsOfHill, long score, long rank, long pot, double potTaxPercent,
                                String joinUrl, List<Integer> confirmedSeatsId) {
        this.buyIn = buyIn;
        this.buyInConfirmed = buyInConfirmed;
        this.timeToStart = timeToStart;
        this.kingsOfHill = kingsOfHill;
        this.score = score;
        this.rank = rank;
        this.pot = pot;
        this.potTaxPercent = potTaxPercent;
        this.joinUrl = joinUrl;
        this.confirmedSeatsId = confirmedSeatsId;
    }

    @Override
    public long getBuyIn() {
        return buyIn;
    }

    @Override
    public void setBuyIn(long buyIn) {
        this.buyIn = buyIn;
    }

    @Override
    public boolean isBuyInConfirmed() {
        return buyInConfirmed;
    }

    @Override
    public void setBuyInConfirmed(boolean buyInConfirmed) {
        this.buyInConfirmed = buyInConfirmed;
    }

    @Override
    public int getTimeToStart() {
        return timeToStart;
    }

    @Override
    public void setTimeToStart(int timeToStart) {
        this.timeToStart = timeToStart;
    }

    @Override
    public List<Integer> getKingsOfHill() {
        return kingsOfHill;
    }

    @Override
    public void setKingsOfHill(List<Integer> kingsOfHill) {
        this.kingsOfHill = kingsOfHill;
    }

    @Override
    public long getScore() {
        return score;
    }

    @Override
    public void setScore(long score) {
        this.score = score;
    }

    @Override
    public long getRank() {
        return rank;
    }

    @Override
    public void setRank(long rank) {
        this.rank = rank;
    }

    @Override
    public long getPot() {
        return pot;
    }

    @Override
    public void setPot(long pot) {
        this.pot = pot;
    }

    @Override
    public double getPotTaxPercent() {
        return potTaxPercent;
    }

    @Override
    public void setPotTaxPercent(double potTaxPercent) {
        this.potTaxPercent = potTaxPercent;
    }

    @Override
    public String getJoinUrl() {
        return joinUrl;
    }

    @Override
    public void setJoinUrl(String joinUrl) {
        this.joinUrl = joinUrl;
    }

    @Override
    public List<Integer> getConfirmedSeatsId() {
        return confirmedSeatsId;
    }

    @Override
    public void setConfirmedSeatsId(List<Integer> confirmedSeatsId) {
        this.confirmedSeatsId = confirmedSeatsId;
    }

    public List<ITransportObserver> getObservers() {
        return observers;
    }

    public void setObservers(List<ITransportObserver> observers) {
        this.observers = observers;
    }

    @Override
    public String toString() {
        return "BattlegroundInfo{" +
                "buyIn=" + buyIn +
                ", buyInConfirmed=" + buyInConfirmed +
                ", timeToStart=" + timeToStart +
                ", kingOfHill=" + kingsOfHill +
                ", score=" + score +
                ", rank=" + rank +
                ", pot=" + pot +
                ", potTaxPercent=" + potTaxPercent +
                ", joinUrl=" + joinUrl +
                ", confirmedSeatsId=" + confirmedSeatsId +
                ", observers=" + observers +
                '}';
    }
}
