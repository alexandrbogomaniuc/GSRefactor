package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

public class CrashBetResponse extends TObject {
    private long crashBetAmount;
    private long balance;
    private String crashBetKey;

    public CrashBetResponse(long date, int rid, long crashBetAmount, long balance, String crashBetKey) {
        super(date, rid);
        this.crashBetAmount = crashBetAmount;
        this.balance = balance;
        this.crashBetKey = crashBetKey;
    }


    public String getCrashBetKey() {
        return crashBetKey;
    }

    public void setCrashBetKey(String crashBetKey) {
        this.crashBetKey = crashBetKey;
    }

    public long getCrashBetAmount() {
        return crashBetAmount;
    }

    public void setCrashBetAmount(long crashBetAmount) {
        this.crashBetAmount = crashBetAmount;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CrashBetResponse that = (CrashBetResponse) o;

        if (crashBetAmount != that.crashBetAmount) return false;
        return balance == that.balance;

    }

    @Override
    public String toString() {
        return "CrashBetResponse[" +
                "crashBetAmount=" + crashBetAmount +
                ", balance=" + balance +
                ", crashBetKey=" + crashBetKey +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
