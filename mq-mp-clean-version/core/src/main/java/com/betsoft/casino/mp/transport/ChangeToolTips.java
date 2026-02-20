package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class ChangeToolTips extends TInboundObject {
    private boolean disableTooltips;

    public ChangeToolTips(long date, int rid, boolean disableTooltips) {
        super(date, rid);
        this.disableTooltips = disableTooltips;
    }

    public boolean isDisableTooltips() {
        return disableTooltips;
    }

    public void setDisableTooltips(boolean disableTooltips) {
        this.disableTooltips = disableTooltips;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChangeToolTips [");
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(", disableTooltips=").append(disableTooltips);
        sb.append(']');
        return sb.toString();
    }
}

