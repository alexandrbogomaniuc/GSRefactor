package com.abs.casino.common.transactiondata;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Created by grien on 05.06.15.
 */
public class TrackingInfo implements KryoSerializable {
    private static final byte VERSION = 2;
    private long accountId;
    private boolean hasWallet;
    private boolean hasFrbWin;
    private boolean hasFrbNotification;
    private boolean hasPaymentTransaction;

    public TrackingInfo() {
    }

    public TrackingInfo(long accountId, boolean hasWallet, boolean hasFrbWin,
                        boolean hasFrbNotification, boolean hasPaymentTransaction) {
        this.accountId = accountId;
        this.hasWallet = hasWallet;
        this.hasFrbWin = hasFrbWin;
        this.hasFrbNotification = hasFrbNotification;
        this.hasPaymentTransaction = hasPaymentTransaction;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public boolean hasWallet() {
        return hasWallet;
    }

    public void setHasWallet(boolean hasWallet) {
        this.hasWallet = hasWallet;
    }

    public boolean hasFrbWin() {
        return hasFrbWin;
    }

    public void setHasFrbWin(boolean hasFrbWin) {
        this.hasFrbWin = hasFrbWin;
    }

    public boolean hasFrbNotification() {
        return hasFrbNotification;
    }

    public void setHasFrbNotification(boolean hasFrbNotification) {
        this.hasFrbNotification = hasFrbNotification;
    }

    public boolean isHasPaymentTransaction() {
        return hasPaymentTransaction;
    }

    public void setHasPaymentTransaction(boolean hasPaymentTransaction) {
        this.hasPaymentTransaction = hasPaymentTransaction;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(accountId, true);
        output.writeBoolean(hasWallet);
        output.writeBoolean(hasFrbWin);
        output.writeBoolean(hasPaymentTransaction);
        output.writeBoolean(hasFrbNotification);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        @SuppressWarnings("UnusedDeclaration")
        byte ver = input.readByte();
        accountId = input.readLong(true);
        hasWallet = input.readBoolean();
        hasFrbWin = input.readBoolean();
        if (ver > 0) {
            hasPaymentTransaction = input.readBoolean();
        }
        if (ver > 1) {
            hasFrbNotification = input.readBoolean();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackingInfo that = (TrackingInfo) o;

        if (accountId != that.accountId) return false;
        if (hasWallet != that.hasWallet) return false;
        if (hasFrbWin != that.hasFrbWin) return false;
        if (hasFrbNotification != that.hasFrbNotification) return false;
        return hasPaymentTransaction == that.hasPaymentTransaction;

    }

    @Override
    public int hashCode() {
        return (int) (accountId ^ (accountId >>> 32));
    }

    @Override
    public String toString() {
        return "TrackingInfo[" +
                "accountId=" + accountId +
                ", hasWallet=" + hasWallet +
                ", hasFrbWin=" + hasFrbWin +
                ", hasFrbNotification=" + hasFrbNotification +
                ", hasPaymentTransaction=" + hasPaymentTransaction +
                ']';
    }
}
