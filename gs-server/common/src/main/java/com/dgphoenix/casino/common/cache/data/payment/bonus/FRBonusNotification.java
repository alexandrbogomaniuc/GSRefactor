package com.abs.casino.common.cache.data.payment.bonus;

import com.abs.casino.common.cache.data.bonus.BonusStatus;
import com.abs.casino.common.cache.data.payment.frb.FRBonusNotificationStatus;
import com.abs.casino.common.util.string.StringUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FRBonusNotification implements KryoSerializable, Serializable {
    private static final byte VERSION = 1;

    private long id;
    private long accountId;
    private long bonusId;
    private String extBonusId;
    private long winSum;
    private long startTime;
    private BonusStatus bonusStatus;
    private FRBonusNotificationStatus externalStatus;

    public FRBonusNotification() {}

    public FRBonusNotification(long id, long accountId, long bonusId, String extBonusId, long winSum,
                               BonusStatus bonusStatus, FRBonusNotificationStatus externalStatus) {
        this.id = id;
        this.accountId = accountId;
        this.bonusId = bonusId;
        this.extBonusId = extBonusId;
        this.winSum = winSum;
        this.startTime = System.currentTimeMillis();
        this.bonusStatus = bonusStatus;
        this.externalStatus = externalStatus;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getBonusId() {
        return bonusId;
    }

    public void setBonusId(long bonusId) {
        this.bonusId = bonusId;
    }

    public String getExtBonusId() {
        return extBonusId;
    }

    public void setExtBonusId(String extBonusId) {
        this.extBonusId = extBonusId;
    }

    public long getWinSum() {
        return winSum;
    }

    public void setWinSum(long winSum) {
        this.winSum = winSum;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public BonusStatus getBonusStatus() {
        return bonusStatus;
    }

    public void setBonusStatus(BonusStatus bonusStatus) {
        this.bonusStatus = bonusStatus;
    }

    public FRBonusNotificationStatus getExternalStatus() {
        return externalStatus;
    }

    public void setExternalStatus(FRBonusNotificationStatus externalStatus) {
        this.externalStatus = externalStatus;
    }

    public boolean isOverdue(long time) {
        return System.currentTimeMillis() - startTime > time;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        return "FRBonusNotification{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", bonusId=" + bonusId +
                ", extBonusId='" + extBonusId + '\'' +
                ", winSum=" + winSum +
                ", startTime=" + formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault())) +
                ", bonusStatus=" + bonusStatus +
                ", externalStatus=" + externalStatus +
                '}';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(accountId, true);
        output.writeLong(bonusId, true);
        output.writeString(extBonusId);
        output.writeLong(winSum, true);
        output.writeLong(startTime, true);
        output.writeString(bonusStatus == null ? BonusStatus.ACTIVE.name() : bonusStatus.name());
        output.writeString(externalStatus == null ? FRBonusNotificationStatus.STARTED.name() : externalStatus.name());
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong(true);
        accountId = input.readLong(true);
        bonusId = input.readLong(true);
        if (ver > 0) {
            extBonusId = input.readString();
        }
        winSum = input.readLong(true);
        startTime = input.readLong(true);
        String s = input.readString();
        bonusStatus = StringUtils.isTrimmedEmpty(s) ? BonusStatus.ACTIVE : BonusStatus.valueOf(s);
        s = input.readString();
        externalStatus = StringUtils.isTrimmedEmpty(s) ? FRBonusNotificationStatus.STARTED :
                FRBonusNotificationStatus.valueOf(s);
    }
}
