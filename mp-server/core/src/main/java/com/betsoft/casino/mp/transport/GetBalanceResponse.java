package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

public class GetBalanceResponse extends TObject {
    private long balance;

    public GetBalanceResponse(long date, int rid, long balance) {
        super(date, rid);
        this.balance = balance;
    }

    public long getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "GetBalanceResponse[" +
                "balance=" + balance +
                ", date=" + date +
                ", rid=" + rid +
                ']';
    }
}
