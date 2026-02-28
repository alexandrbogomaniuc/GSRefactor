package com.abs.casino.common.cache.data.report;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.abs.casino.common.util.ReportPeriodEnum;
import com.abs.casino.common.util.ReportTypeEnum;

import java.util.Date;

public class PeriodicReportInfo implements IDistributedCacheEntry {
    private final long gameServerId;
    private final long bankId;
    private final ReportPeriodEnum period;
    private final ReportTypeEnum type;
    private long updateDate;
    private boolean sended;

    public PeriodicReportInfo(long gameServerId, long bankId, ReportPeriodEnum period, ReportTypeEnum type) {
        this.gameServerId = gameServerId;
        this.bankId = bankId;
        this.period = period;
        this.type = type;
    }

    public long getGameServerId() {
        return gameServerId;
    }

    public long getBankId() {
        return bankId;
    }

    public ReportPeriodEnum getPeriod() {
        return period;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public ReportTypeEnum getType() {
        return type;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public boolean isSended() {
        return sended;
    }

    public void setSended(boolean sended) {
        this.sended = sended;
    }

    public boolean isNeedUpdate() {
        return System.currentTimeMillis() - updateDate > period.getPeriodInMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeriodicReportInfo that = (PeriodicReportInfo) o;

        if (bankId != that.bankId) return false;
        if (gameServerId != that.gameServerId) return false;
        if (period != that.period) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (gameServerId ^ (gameServerId >>> 32));
        result = 31 * result + (int) (bankId ^ (bankId >>> 32));
        result = 31 * result + (period != null ? period.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PeriodicReportInfo");
        sb.append("[gameServerId=").append(gameServerId);
        sb.append(", bankId=").append(bankId);
        sb.append(", period=").append(period);
        sb.append(", type=").append(type);
        sb.append(", updateDate=").append(new Date(updateDate));
        sb.append(", sended=").append(sended);
        sb.append(", isNeedUpdate=").append(isNeedUpdate());
        sb.append(']');
        return sb.toString();
    }
}
