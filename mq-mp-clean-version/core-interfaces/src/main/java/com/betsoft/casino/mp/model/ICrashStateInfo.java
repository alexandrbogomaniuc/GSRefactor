package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

public interface ICrashStateInfo extends ITransportObject, IServerMessage {
    double getCurrentMult();
    void setCurrentMult(double currentMult);
    double getTimeSpeedMult();
    void setTimeSpeedMult(double timeSpeedMult);
    void setCrash(Boolean crash);
    void setAsteroid(ITransportAsteroid asteroid);
    void setAllEjectedTime(Long allEjectedTime);
}
