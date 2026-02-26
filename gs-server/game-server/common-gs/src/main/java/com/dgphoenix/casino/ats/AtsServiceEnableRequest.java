package com.abs.casino.ats;

public class AtsServiceEnableRequest {
    private boolean enable;

    public AtsServiceEnableRequest() {

    }

    public AtsServiceEnableRequest(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        return "AtsServiceEnableRequest{" +
                "enable=" + enable +
                '}';
    }
}
