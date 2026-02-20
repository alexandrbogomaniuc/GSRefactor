package com.betsoft.casino.mp.model;

import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IPlayerBet extends KryoSerializable {
    long getDateTime();

    void setDateTime(long dateTime);

    double getBet();

    void setBet(double bet);

    double getWin();

    void setWin(double win);

    String getData();

    void setData(String data);

    long getAccountId();

    void setAccountId(long accountId);

    void setStartRoundTime(long roundStartTime);

    long getStartRoundTime();

    void addData(String newData);
}
