package com.betsoft.casino.mp.model.battleground;

import com.betsoft.casino.mp.model.privateroom.Status;

public interface ITransportObserver {

    String getNickname();

    void setNickname(String nickname);

    boolean isKicked();

    void setKicked(boolean kicked);

    Status getStatus();

    void setStatus(Status status);

    Boolean isOwner();

    void setOwner(Boolean isOwner);
}
