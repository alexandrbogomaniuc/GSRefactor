package com.betsoft.casino.bots;

import java.util.StringJoiner;

public class AstronautBetData {
    private String ejectBetId;
    private double multiplierForCancelOrEject;
    private boolean needAutoEject;
    private boolean cancelled;

    public AstronautBetData(String ejectBetId, double multiplierForCancelOrEject, boolean needAutoEject) {
        this.ejectBetId = ejectBetId;
        this.multiplierForCancelOrEject = multiplierForCancelOrEject;
        this.needAutoEject = needAutoEject;
        this.cancelled = false;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isNeedAutoEject() {
        return needAutoEject;
    }

    public void setNeedAutoEject(boolean needAutoEject) {
        this.needAutoEject = needAutoEject;
    }

    public String getEjectBetId() {
        return ejectBetId;
    }

    public void setEjectBetId(String ejectBetId) {
        this.ejectBetId = ejectBetId;
    }

    public double getMultiplierForCancelOrEject() {
        return multiplierForCancelOrEject;
    }

    public void setMultiplierForCancelOrEject(double multiplierForCancelOrEject) {
        this.multiplierForCancelOrEject = multiplierForCancelOrEject;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", AstronautBetData.class.getSimpleName() + "[", "]")
                .add("ejectBetId='" + ejectBetId + "'")
                .add("multiplierForCancelOrEject=" + multiplierForCancelOrEject)
                .add("needAutoEject=" + needAutoEject)
                .add("cancelled=" + cancelled)
                .toString();
    }
}
