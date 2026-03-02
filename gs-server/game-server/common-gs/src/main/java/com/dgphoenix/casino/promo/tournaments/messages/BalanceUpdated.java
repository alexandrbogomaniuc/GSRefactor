package com.abs.casino.promo.tournaments.messages;

import com.abs.casino.common.transport.TObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 14.08.2020.
 */
public class BalanceUpdated extends TObject {
    private static final byte VERSION = 0;
    private long balance;

    public BalanceUpdated() {}

    public BalanceUpdated(long date, int rid, long balance) {
        super(date, rid);
        this.balance = balance;
    }

    public BalanceUpdated(long balance) {
        this(System.currentTimeMillis(), TObject.SERVER_RID, balance);
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    protected byte getVersion() {
        return VERSION;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeLong(balance, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        balance = input.readLong(true);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BalanceUpdated [");
        sb.append("balance=").append(balance);
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
