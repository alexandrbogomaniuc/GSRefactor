package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.privateroom.Status;

public interface IBGPlayer {
    public String getNickname();

    public long getAccountId();

    public String getExternalId();

    public Status getStatus();
}
