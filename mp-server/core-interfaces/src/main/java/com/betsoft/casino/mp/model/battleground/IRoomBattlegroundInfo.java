package com.betsoft.casino.mp.model.battleground;

import java.util.List;

public interface IRoomBattlegroundInfo {

    long getBuyIn();

    void setBuyIn(long buyIn);

    boolean isBuyInConfirmed();

    void setBuyInConfirmed(boolean buyInConfirmed);

    int getTimeToStart();

    void setTimeToStart(int timeToStart);

    List<Integer> getKingsOfHill();

    void setKingsOfHill(List<Integer> kingsOfHill);

    long getScore();

    void setScore(long score);

    long getRank();

    void setRank(long rank);

    long getPot();

    void setPot(long pot);

    double getPotTaxPercent();

    void setPotTaxPercent(double potTaxPercent);

    String getJoinUrl();

    void setJoinUrl(String joinUrl);

    List<Integer> getConfirmedSeatsId();

    void setConfirmedSeatsId(List<Integer> confirmedSeatsId);

    List<ITransportObserver> getObservers();

    void setObservers(List<ITransportObserver> observers);
}
