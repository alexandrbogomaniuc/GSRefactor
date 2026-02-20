package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IFRBEnded;
import com.betsoft.casino.utils.TObject;

/**
 * User: flsh
 * Date: 03.08.18.
 */
public class FRBEnded extends TObject implements IFRBEnded {
    private long winSum;
    private String closeReason;
    private boolean hasNextFrb;
    private long realWinSum;

    public FRBEnded(long date, long winSum, String closeReason, boolean hasNextFrb, long realWinSum) {
        super(date, SERVER_RID);
        this.winSum = winSum;
        this.closeReason = closeReason;
        this.hasNextFrb = hasNextFrb;
        this.realWinSum = realWinSum;
    }

    public long getWinSum() {
        return winSum;
    }

    public void setWinSum(long winSum) {
        this.winSum = winSum;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public void setCloseReason(String closeReason) {
        this.closeReason = closeReason;
    }

    public boolean isHasNextFrb() {
        return hasNextFrb;
    }

    public void setHasNextFrb(boolean hasNextFrb) {
        this.hasNextFrb = hasNextFrb;
    }

    @Override
    public long getRealWinSum() {
        return realWinSum;
    }

    public void setRealWinSum(long realWinSum) {
        this.realWinSum = realWinSum;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FRBEnded [");
        sb.append("winSum=").append(winSum);
        sb.append(", closeReason='").append(closeReason).append('\'');
        sb.append(", date=").append(date);
        sb.append(", hasNextFrb=").append(hasNextFrb);
        sb.append(", realWinSum=").append(realWinSum);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
