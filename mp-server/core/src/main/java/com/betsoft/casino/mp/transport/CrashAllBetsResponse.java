package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ICrashAllBets;
import com.betsoft.casino.utils.TObject;

public class CrashAllBetsResponse extends TObject  implements ICrashAllBets {

    private int seatId;
    private String name;
    private long balance;
    private long amount;

    public CrashAllBetsResponse(long date, int rid, int seatId, String name, long balance, long amount) {
        super(date, rid);
        this.seatId = seatId;
        this.name = name;
        this.balance = balance;
        this.amount = amount;
    }

    @Override
    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
