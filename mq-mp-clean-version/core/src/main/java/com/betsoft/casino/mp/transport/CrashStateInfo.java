package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ICrashStateInfo;
import com.betsoft.casino.mp.model.ITransportAsteroid;
import com.betsoft.casino.utils.TObject;

public class CrashStateInfo extends TObject implements ICrashStateInfo {
    private double currentMult;
    private double timeSpeedMult;
    private Boolean crash;
    private Long allEjectedTime;
    private ITransportAsteroid asteroid;


    public CrashStateInfo(long date, double currentMult, double timeSpeedMult) {
        super(date, SERVER_RID);
        this.currentMult = currentMult;
        this.timeSpeedMult = timeSpeedMult;
    }

    @Override
    public double getCurrentMult() {
        return currentMult;
    }

    @Override
    public void setCurrentMult(double currentMult) {
        this.currentMult = currentMult;
    }

    @Override
    public double getTimeSpeedMult() {
        return timeSpeedMult;
    }

    @Override
    public void setTimeSpeedMult(double timeSpeedMult) {
        this.timeSpeedMult = timeSpeedMult;
    }

    public Boolean getCrash() {
        return crash;
    }

    @Override
    public void setCrash(Boolean crash) {
        this.crash = crash;
    }

    public ITransportAsteroid getAsteroid() {
        return asteroid;
    }

    @Override
    public void setAsteroid(ITransportAsteroid asteroid) {
        this.asteroid = asteroid;
    }

    public Long getAllEjectedTime() {
        return allEjectedTime;
    }

    @Override
    public void setAllEjectedTime(Long allEjectedTime) {
        this.allEjectedTime = allEjectedTime;
    }

    @Override
    public String toString() {
        return "CrashStateInfo{" +
                "currentMult=" + currentMult +
                ", timeSpeedMult=" + timeSpeedMult +
                ", crash=" + crash +
                ", allEjectedTime=" + allEjectedTime +
                ", asteroid=" + asteroid +
                '}';
    }
}
