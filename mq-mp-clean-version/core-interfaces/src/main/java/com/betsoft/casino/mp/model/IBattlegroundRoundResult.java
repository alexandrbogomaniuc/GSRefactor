package com.betsoft.casino.mp.model;

public interface IBattlegroundRoundResult {

    long getId();

    void setId(long id);

    long getScore();

    void setScore(long score);

    long getRank();

    void setRank(long rank);

    long getPot();

    void setPot(long pot);

    String getNickName();

    void setNickName(String nickName);
}
